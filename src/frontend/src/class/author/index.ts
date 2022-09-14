/**
 * 새로운 Author 객체를 생성함.
 * @class
 * @implements {IAuthor}
 */
export class Author implements IAuthor {
  id?: number | null;
  name?: string;

  /**
   * @constructor
   * @param id DB에 저장된 저자의 정보를 식별하기 위한 고유의 ID 값
   * @param name 저자명
   */
  constructor(id?: number | null, name?: string) {
    this.id = id;
    this.name = name;
  }
}

/**
 * @interface
 */
export interface IAuthor {
  id?: number | null;
  name?: string;
}
