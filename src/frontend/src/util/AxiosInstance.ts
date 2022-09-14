import axios, { AxiosInstance } from 'axios';
import { useTokenStore } from 'src/stores/Token';

/**
 * Axios 통신 도중, AxiosInstance를 가로채고 작업을 하는 함수.
 * @private
 * @param instance 도중에 가로챈 AxiosInstance 객체
 * @returns 작업이 완료 된 AxiosInstance 객체
 */
function setInterceptors(instance: AxiosInstance) {
  const token = useTokenStore();

  // 어떤 요청(request)이 있기 전에 인터셉트 하는 부분
  instance.interceptors.request.use(

    // 설정
    (config) => {
      config.headers['Authorization'] = token.getAccessToken;
      config.headers['Authorization-refresh'] = token.getRefreshToken;
      return config;
    },

    // 에러
    (error) => {
      return Promise.reject(error);
    },
  );

  // 어떤 응답(reponse)이 도달하기 전에 인터셉트 하는 부분
  instance.interceptors.response.use(

    // 응답
    (response) => {
      const headers = response.headers;
      const accessToken = headers['authorization'];
      const refreshToken = headers['authorization-refresh'] ? headers['authorization-refresh'] : token.getRefreshToken;

      token.setToken(accessToken, refreshToken);
      token.setRefreshTokenOnCookies(refreshToken);

      return response;
    },

    // 에러
    (error) => {
      const status = error.response.status;
      const headers = error.response.headers;

      if(status === 417) {
        const accessToken = headers['authorization'];
        const refreshToken = headers['authorization-refresh'] ? headers['authorization-refresh'] : token.getRefreshToken;

        token.setToken(accessToken, refreshToken);
        token.setRefreshTokenOnCookies(refreshToken);
      }

      return Promise.reject(error);
    },
  );

  return instance;
}

/**
 * 새로운 AxiosInstance를 생성하는 함수
 * @private
 * @returns 새로 생성된 AxiosInstance
 */
function createAxiosInstance() {
  return axios.create();
}

/**
 * 인증 정보가 담긴 새로운 AxiosInstance를 생성하는 함수
 * @private
 * @returns 새로 생성된 AxiosInstance
 */
function createAxiosInstanceWithAuth() {
  return setInterceptors(axios.create());
}

export const axiosInstance = createAxiosInstance();
export const axiosInstanceWithAuth = createAxiosInstanceWithAuth();
