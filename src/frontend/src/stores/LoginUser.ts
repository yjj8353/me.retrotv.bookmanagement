import { AxiosResponse } from 'axios';
import { defineStore } from 'pinia';
import { IMember, MemberBuilder } from 'src/class/member';
import { Response } from 'src/class/response';
import { KeyType, Result } from 'src/class/result';
import { axiosInstance, axiosInstanceWithAuth } from 'src/util/AxiosInstance';

import { useTokenStore } from './Token';

export const useLoginUser = defineStore('loginUser', {
  state: () => ({
    token: useTokenStore(),
    authenticated: useTokenStore().existToken,
    username: null as unknown as string
  }),

  getters: {
    isAuthenticated: state => state.authenticated,
    getUsername: state => state.username
  },

  actions: {

    /**
     * 로그인을 백엔드에 요청하는 함수.
     * @async
     * @param id 계정
     * @param password 패스워드
     * @return 로그인 성공 여부 및 메시지가 담긴 Promise<Result> 객체
     */
    async login(id: string, password: string): Promise<Result> {
      const result = new Result();

      if(!id || !password) {
        result.add(KeyType.SUCCESS, false);
        result.add(KeyType.MESSAGE, '아이디 혹은 패스워드가 빈칸일 수 없습니다.');
        return result;
      }

      const axiosResponse = await axiosInstance.post('/api/member/login', {
        headers: {
          'content-type': 'application/json'
        },
        data: { 'username': id, 'password': password }
      }).catch((error) => {
        return error.response;
      });

      if(axiosResponse.status === 200) {
        const accessToken = axiosResponse.headers['authorization'];
        const refreshToken = axiosResponse.headers['authorization-refresh'];
        this.token.setToken(accessToken, refreshToken);
        this.token.setRefreshTokenOnCookies(refreshToken);

        result.add(KeyType.SUCCESS, true);
        return result;
      } else {
        const response = new Response(axiosResponse);
        result.add(KeyType.SUCCESS, false);
        result.add(KeyType.MESSAGE, response.message);
        return result;
      }
    },

    /**
     * 회원가입을 백엔드에 요청하는 함수.
     * @async
     * @param username 계정
     * @param password 패스워드
     * @param passwordCheck 회원가입 시 패스워드를 체크하기 위한 값
     * @param realName 실명
     * @param email 이메일
     * @returns 회원가입 성공 여부 및 메시지가 담긴 Promise<Result> 객체
     */
    async join(username: string, password: string, passwordCheck: string, realName: string, email: string): Promise<Result> {
      const result = new Result();
      const member: IMember = new MemberBuilder().username(username)
                                                 .password(password)
                                                 .passwordCheck(passwordCheck)
                                                 .realName(realName)
                                                 .email(email)
                                                 .build();

      const axiosResponse = await axiosInstance.post('/api/member/join', member, {
        headers: {
          'content-type': 'application/json'
        }
      }).catch((error) => {
        if(error.response.status === 409 || error.response.status === 400) {
          result.add(KeyType.SUCCESS, error.response.data.success);
          result.add(KeyType.MESSAGE, error.response.data.message);
          return result;
        }
      });

      if(axiosResponse instanceof Result) {
        return axiosResponse;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        return result;
      }
    },

    /**
     * 로그아웃을 백엔드에 요청하는 함수
     * @async
     * @returns 로그아웃 성공 여부 및 메시지가 담긴 Promise<Result> 객체
     */
    async logout(): Promise<Result> {
      const result = new Result();
      const member: IMember = new MemberBuilder().refreshToken(this.token.getRefreshToken).build();

      const axiosResponse = await axiosInstance.post('/api/member/logout', member, {
        headers: {
          'content-type': 'application/json'
        }
      });

      const response = new Response(axiosResponse as unknown as AxiosResponse);
      const success = response.success;
      const message = response.message;

      if(success) {
        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        this.username = '';
        this.token.destroyToken();
      }

      return result;
    },

    async passwordChange(orgPassword: string, newPassword: string, newPasswordCheck: string, refreshToken: string, passcode: string): Promise<Result> {
      const result = new Result();
      let url = '';

      if(orgPassword) {
        url = '/api/member/password-change-with-auth';
      }

      if(passcode) {
        url = '/api/member/password-change-none-auth';
      }

      if(newPassword !== newPasswordCheck) {
        result.add(KeyType.SUCCESS, false);
        result.add(KeyType.MESSAGE, '새로운 패스워드와 새로운 패스워드 확인 값이 다릅니다.');

        return result;
      }

      const member = {
        orgPassword: orgPassword,
        newPassword: newPassword,
        newPasswordCheck: newPasswordCheck,
        refreshToken: refreshToken,
        passcode: passcode
      }

      const axiosResponse = await axiosInstanceWithAuth.post(url, member, {
        headers: {
          'content-type': 'application/json'
        }
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.passwordChange(orgPassword, newPassword, newPasswordCheck, refreshToken, passcode);
        } else {
          result.add(KeyType.SUCCESS, error.response.data.success);
          result.add(KeyType.MESSAGE, error.response.data.message);
          return result;
        }
      });

      if(axiosResponse instanceof Result) {
        return axiosResponse;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        return result;
      }
    },

    async sendEmailPasswordChange(email: string): Promise<Response> {
      const axiosResponse = await axiosInstance.get('/api/member/password-change-email-send', {
        headers: {
          'Content-Type': 'application/json',
        },
        params: { email: email }
      }).catch((error) => {
        if(error.response.status === 404 || error.response.status === 500) {
          return new Response(error.response);
        }
      });

      let response;
      if(axiosResponse instanceof Response) {
        response = axiosResponse;
      } else {
        response = new Response(axiosResponse as unknown as AxiosResponse);
      }

      return response
    }
  }
});
