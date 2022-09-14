/**
 * 새로운 Image 객체를 생성함.
 * @class
 * @implements {IImage}
 */
export class Image implements IImage {
  id?: number | null;
  name?: string;
  proxyName?: string;
  format?: string;
  path?: string;

  /**
   * @constructor
   * @param id DB에 저장된 책 표지 정보를 식별하기 위한 고유의 ID 값
   * @param name 이미지의 실제 파일명
   * @param proxyName 저장시 중복 파일명 문제를 피하기 위해 임의로 부여한 고유의 파일명
   * @param format 확장자명
   * @param path 저장경로
   */
  constructor(
    id?: number | null,
    name?: string,
    proxyName?: string,
    format?: string,
    path?: string
  ) {
    this.id = id;
    this.name = name;
    this.proxyName = proxyName;
    this.format = format;
    this.path = path;
  }
}

/**
 * @interface
 */
export interface IImage {
  id?: number | null
  name?: string
  proxyName?: string
  format?: string
  path?: string
}
