/**
 * 새로운 Publisher 객체를 생성함.
 * @class
 * @implements {IPublisher}
 */
export class Publisher implements IPublisher {
  id?: number | null;
  name?: string;

  /**
   * @constructor
   * @param id DB에 저장된 출판사 정보를 식별하기 위한 고유의 ID 값
   * @param name 출판사명
   */
  constructor(id?: number | null, name?: string) {
    this.id = id;
    this.name = name;
  }
}

/**
 * @interface
 */
export interface IPublisher {
  id?: number | null;
  name?: string
}
