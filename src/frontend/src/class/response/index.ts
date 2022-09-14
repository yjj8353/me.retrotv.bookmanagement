/* eslint-disable @typescript-eslint/no-explicit-any */
import { AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * 백엔드에서 전달하는 ResponseEntity 객체를 용이하게 처리하기 위한 AxiosResponse의 구현체.
 * @class
 */
export class Response implements AxiosResponse {
  success: boolean;
  message: string;
  data: any;
  dataCount: number;
  status: number;
  statusText: string;
  headers: any;
  config: AxiosRequestConfig;
  request?: any;

  /**
   * @constructor
   * @param response Axios 요청을 전송하고 받은 응답 데이터
   */
  constructor(response: AxiosResponse<any>) {
    this.success = response.data.success;
    this.message = response.data.message;
    this.data = response.data.data;
    this.dataCount = response.data.count;
    this.status = response.status;
    this.statusText = response.statusText;
    this.headers = response.headers;
    this.config = response.config;
    this.request = response.request;
  }
}
