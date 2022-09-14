import axios from 'axios';
import { defineStore } from 'pinia';
import { Cookies } from 'quasar';
import { Response } from 'src/class/response';

const BEARER = 'Bearer ';

export const useTokenStore = defineStore('token', {
  state: () => ({
    accessToken: '',
    refreshToken: ''
  }),

  getters: {
    existAccessToken: state => state.accessToken ? true : false,
    existRefreshToken: state => state.refreshToken ? true : false,
    existToken: state => (state.accessToken || state.refreshToken) ? true : false,
    getAccessToken: state => BEARER + state.accessToken,
    getRefreshToken: state => BEARER + state.refreshToken
  },

  actions: {

    /**
     * 백엔드를 통해 발급받은 JWT Token을 세팅하는 함수.
     * @param accessToken 백엔드에서 발급받은 JWT Access Token
     * @param refreshToken 백엔드에서 발급받은 JWT Refresh Token
     */
    setToken(accessToken: string, refreshToken: string): void {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
    },

    /**
     * 쿠키에 저장된 Token을 포함해, 발급받은 모든 JWT Token을 파기하는 함수.
     */
    destroyToken(): void {
      this.accessToken = '';
      this.refreshToken = '';
      Cookies.remove('refreshToken');
    },

    /**
     * 쿠키에 JWT Refresh Token을 세팅하는 함수.
     * @param refreshToken 백엔드에서 발급받은 JWT Refresh Token
     */
    setRefreshTokenOnCookies(refreshToken: string): void {
      Cookies.set('refreshToken', refreshToken, {
        expires: '3d', // 세팅 된 후 3일뒤 파기
        secure: true, // https 프로토콜 필수
        sameSite: 'Strict'
      });
    },

    /**
     * 쿠키에 저장된 JWT Refresh Token을 가져와 store의 refreshToken 변수에 세팅하는 함수.
     */
    getRefreshTokenFromCookies(): void {
      this.refreshToken = Cookies.get('refreshToken') ? Cookies.get('refreshToken') : '';
    },

    /**
     * 토큰이 유효한지 백엔드에 요청하는 함수.
     */
    checkTokenValid(): void {
      if(this.refreshToken === '') {
        return;
      }

      const jwt = {
        username: null,
        accessToken: this.accessToken,
        refreshToken: this.refreshToken
      }

      /*
       * axiosInstance를 사용하면 오류가 발생함.
       * Vue lifecycle 상, Token store가 설정 되기전에 실행되서 그런것으로 보임.
       */
      axios.post('/api/jwt/valid', jwt, {
        headers: {
          'Content-Type': 'application/json',
        }
      }).then((axiosReponse) => {
        const response = new Response(axiosReponse);
        const isValid = response.data.isValid;
        if(isValid) {
          const accessToken = response.headers['authorization'];
          const refreshToken = response.headers['authorization-refresh'];
          this.setToken(accessToken, refreshToken);
          this.setRefreshTokenOnCookies(refreshToken);
        }
      }).catch((error) => {
        const status = error.response.status;
        if(status === 401) {
          this.accessToken = '';
          this.refreshToken = '';
          Cookies.remove('refreshToken');
        }
      })
    }
  }
})
