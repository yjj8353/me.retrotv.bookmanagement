import { AxiosResponse } from 'axios';
import { defineStore } from 'pinia';
import { IMember } from 'src/class/member';
import { Response } from 'src/class/response';
import { KeyType, Result } from 'src/class/result';
import { axiosInstanceWithAuth } from 'src/util/AxiosInstance';

export const useAdminStore = defineStore('admin', {
  state: () => ({
    members: null as unknown as IMember[]
  }),

  getters: {

  },

  actions: {

    /**
     * 인증되지 않은 계정들의 목록을 가져오도록 백엔드에 요청하는 함수.
     * @deprecated 사용하지 않을 예정
     * @async
     */
    async getNotCertifiedMemebers() {
      const axiosReponse = await axiosInstanceWithAuth.get(
        '/api/member/not-certified'
      ).catch((error) => {
        if(error.response.status === 417) {
          return this.getNotCertifiedMemebers();
        }
      });

      try {
        const result = new Response(axiosReponse as unknown as AxiosResponse);
        this.members = result.data;
      } catch(error) {
        if(error instanceof TypeError) {
          this.members = axiosReponse as unknown as IMember[];
        }
      }
    },

    /**
     * 계정 인증을 백엔드에 요청하는 함수.
     * @async
     * @param username 계정
     * @param passcode 인증용코드
     * @returns 회원인증 성공 여부 및 메시지가 담긴 Promise<Result> 객체
     */
    async certifyMember(username: string, passcode: string): Promise<Result> {
      const result = new Result();

      const axiosReponse = await axiosInstanceWithAuth.get('/api/member/certify', {
        headers: {
          'Content-Type': 'application/json',
        },
        params: { 'username': username, 'passcode': passcode }
      }).catch((error) => {
        if(error.response.status === 400) {
          result.add(KeyType.SUCCESS, error.response.data.success);
          result.add(KeyType.MESSAGE, error.response.data.message);
          console.log(result)
          return result;
        }

        if(error.response.status === 417) {
          return this.certifyMember(username, passcode);
        }
      });

      if(axiosReponse instanceof Result) {
        return axiosReponse;
      } else {
        const response = new Response(axiosReponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);
      }

      return result;
    }
  }
});
