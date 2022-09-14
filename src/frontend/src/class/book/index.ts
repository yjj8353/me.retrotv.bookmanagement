import { AxiosResponse } from 'axios';

import { Author, IAuthor } from 'src/class/author';
import { Image, IImage } from 'src/class/image';
import { Publisher, IPublisher } from 'src/class/publisher';

/**
 * 새로운 Book 객체를 생성함.
 * @class
 * @implements {IBook}
 */
export class Book implements IBook {
  id?: number | null;
  title?: string;
  isbn?: string;
  authors?: Array<IAuthor>;
  publisher?: IPublisher;
  image?: IImage;

  /**
   * @constructor
   * @param id DB에 저장된 책의 정보를 식별하기 위한 고유의 ID 값
   * @param title 제목
   * @param isbn ISBN 넘버
   * @param authors 책 저자들의 정보가 담긴 Array<Author> 객체
   * @param publisher 책 출판사의 정보가 담긴 Publisher 객체
   * @param image 책 표지의 정보가 담긴 Image 객체
   */
  constructor(
    id?: number | null,
    title?: string,
    isbn?: string,
    authors?: Array<IAuthor>,
    publisher?: IPublisher,
    image?: IImage
  ) {
    this.id = id;
    this.title = title;
    this.isbn = isbn;
    this.authors = authors;
    this.publisher = publisher;
    this.image = image;
  }

  /**
   * VModel store의 searchBook 요청에 대해 돌려받은 AxiosReponse 객체를 Book 객체로 변환해주는 함수.
   * @param response Book Search 요청을 통해 돌려 받은 AxiosReponse 객체
   * @returns AxiosReponse 객체를 Book 객체로 변환한 결과물
   */
  axiosReponseToBook(response: AxiosResponse) {
    const book = response as unknown as IBook;

    this.id = book.id;
    this.title = book.title;
    this.isbn = book.isbn;

    this.authors = new Array<IAuthor>();

    book.authors?.forEach((author) => {
      const newAuthor: IAuthor = new Author(author.id, author.name);
      this.authors?.push(newAuthor);
    });

    const newPublisher = new Publisher(
      book.publisher?.id,
      book.publisher?.name
    );
    this.publisher = newPublisher;

    const newImage = new Image(
      book.image?.id,
      book.image?.name,
      book.image?.proxyName,
      book.image?.path,
      book.image?.format
    );
    this.image = newImage;

    return this;
  }
}

/**
 * @interface
 */
export interface IBook {
  id?: number | null;
  title?: string;
  isbn?: string;
  authors?: Array<IAuthor>;
  publisher?: IPublisher;
  image?: IImage;

  axiosReponseToBook?: (response: AxiosResponse) => IBook;
}
