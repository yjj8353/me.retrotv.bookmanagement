import { defineStore } from 'pinia';
import { AxiosResponse } from 'axios';
import { Response } from 'src/class/response';
import { Book, IBook } from 'src/class/book';
import { KeyType, Result } from 'src/class/result';
import { useLoginUser } from './LoginUser';
import { axiosInstance, axiosInstanceWithAuth } from 'src/util/AxiosInstance';
import { Author } from 'src/class/author';
import { XMLParser } from 'fast-xml-parser';
import { useTokenStore } from './Token';

export const useVModelStore = defineStore('vModel', {
  state: () => ({
    token: useTokenStore(),
    loginUser: useLoginUser(),

    // SaveAndSearch: save
    saveTitle: '',
    saveIsbn: '',
    saveAuthorName: '',
    savePublisherName: '',
    saveFile: null,
    saveImageLink: '',

    // SaveAndSearch: search
    searchTitle: '',
    searchAuthorName: '',
    searchPublisherName: '',

    // SearchResult, ResultDetail
    searchBooks: [] as unknown as IBook[],
    imageUrls: [] as string[],

    // ModifyBookInfo
    newTitle: '',
    newIsbn: '',
    newAuthorName: '',
    newPublisherName: '',
    newImageFile: null,

    orgTitle: '',
    orgIsbn: '',
    orgAuthorName: '',
    orgPublisherName: '',
    orgImageUrl: '',

    // Pageable
    size: 10,
    page: 0,
    pageSize: 0
  }),

  getters: {},

  actions: {

    /**
     * VModel store를 초기화 하는 함수.
     */
    reset(): void {
      this.$reset();
    },

    /**
     * 책 조회를 백엔드에 요청하는 함수.
     * @async
     * @returns 책 조회 성공여부
     */
    async searchBook(): Promise<boolean> {
      const url = '/api/book/search';
      const book = {
        title: this.searchTitle,
        authorName: this.searchAuthorName,
        publisherName: this.searchPublisherName,
        refreshToken: this.token.refreshToken,
        size: this.size,
        page: this.page,
        pageSize: 1
      };

      const response = new Response(await axiosInstance.get(url, {
        headers: {
          'Content-Type': 'application/json',
        },
        params: book,
      }));

      if(response.data) {

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        response.data.forEach((data: any) => {
          const parsingBook: IBook = new Book().axiosReponseToBook(data);
          this.searchBooks.push(parsingBook);
        });

        this.pageSize = response.dataCount;

        return true;
      }

      return false;
    },

    /**
     * 책 저장을 백엔드에 요청하는 함수.
     * @async
     * @param imageId saveImage 함수에서 DB에 선행 저장된 책 표지의 고유 ID 값
     * @returns Result 객체
     */
    async saveBook(imageId: number | null): Promise<Result> {
      const result = new Result();

      if(!this.saveTitle) {
        result.add(KeyType.SUCCESS, false);
        result.add(KeyType.MESSAGE, '제목은 빈칸일 수 없습니다.');

        return result;
      }

      const authorNamesString = this.saveAuthorName.replaceAll('-', '');
      const authorNames = authorNamesString.split(',');
      const authorList: Author[] = [];

      authorNames.forEach((authorName: string) => {
        authorList.push(new Author(null, authorName));
      });

      const book: IBook = {
        title: this.saveTitle,
        isbn: this.saveIsbn.replaceAll('-', ''),
        authors: authorList,
        publisher: {
          name: this.savePublisherName,
        },
        image: {
          id: imageId,
        },
      };

      const axiosReponse = await axiosInstanceWithAuth.post('/api/book/save', book, {
        headers: {
          'Content-Type': 'application/json',
        },
      }).catch((error) => {

        /*
         * 417(예상 실패) 에러 발생시, accessToken 만료로 인한 에러로 판단,
         * 새로운 accessToken을 발급받아 이 메소드를 재귀처리 한다. (새로운 accessToken은 axios interrupt에서 처리한다)
         */
        if(error.response.status === 417) {
          return this.saveBook(imageId);
        }

        if(error.response.status === 409 || error.response.status === 400) {
          const result = new Result();
          result.add(KeyType.SUCCESS, error.response.data.success);
          result.add(KeyType.MESSAGE, error.response.data.message);

          return result;
        }
      });

      // response가 catch 절에서 return 받은 result 라면 그대로 해당 값을 return 시킨다.
      if(axiosReponse instanceof Result) {
        return axiosReponse;
      } else {
        const response = new Response(axiosReponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        if(success) {
          this.clearQinput();
        }

        return result;
      }
    },

    /**
     * 책 표지 저장을 백엔드에 요청하는 함수.
     * @async
     * @returns DB에 저장되고 부여받은 해당 이미지의 고유 ID 값 (없을 경우 null)
     */
    async saveImage(): Promise<number | null> {
      if(!this.saveFile) {
        return null;
      }

      const formData = new FormData();
      formData.append('files', this.saveFile);
      const url = '/api/image/save';

      const axiosResponse = await axiosInstanceWithAuth.post(url, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.saveImage();
        } else {
          return null;
        }
      });

      if(typeof axiosResponse === 'number') {
        return axiosResponse;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse);
        return response.data.imageId;
      }
    },

    /**
     * Naver API를 통해 받은 표지 정보를 백엔드에서 저장하도록 요청하는 함수.
     * @async
     * @returns DB에 저장되고 부여받은 해당 이미지의 고유 ID 값 (없을 경우 null)
     */
    async saveImageByUrl(): Promise<number | null> {
      if(!this.saveImageLink) {
        return null;
      }

      const url = '/api/image/save-url';
      const axiosResponse = await axiosInstanceWithAuth.post(url, null, {
        headers: {
          'Content-Type': 'application/json',
        },
        params: { url: this.saveImageLink },
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.saveImage();
        } else if(error.response.status === 400) {
          return error.response;
        } else {
          return null;
        }
      });

      if(typeof axiosResponse === 'number') {
        return axiosResponse;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse);
        return response.data.imageId;
      }
    },

    /**
     * 해당 책 정보중, 표지 데이터를 다운로드 하는 함수.
     * @async
     * @param book 표지 데이터를 다운로드 하고자 하는 책 정보가 담긴 Book class 객체
     * @param index Book class 객체가 Array 형태일 경우, 각 Book class 객체의 순번
     */
    async downloadImage(book: IBook, index: number): Promise<void> {
      if(this.imageUrls[index]) {
        return;
      }

      const axiosResponse = await axiosInstance.get('/api/image/download', {
        headers: {
          'Content-Type': 'application/json',
        },
        params: { imageId: book.image?.id },
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

      this.imageUrls[index] = response.data.imageData as string;
    },

    /**
     * ModifyBookInfo 페이지에서 작성된 내용을 기반으로 DB에 해당 Book 객체를 덮어쓰도록 백엔드에 요청한다.
     * @async
     * @param book ResultDetail 페이지로부터 넘겨받은 책 정보가 담긴 원본 Book 객체
     * @returns Result 객체
     */
    async modifyBook(book: Book): Promise<Result> {
      const authorNamesString = this.newAuthorName.replace(' ', '') ? this.newAuthorName.replace(' ', '') : this.orgAuthorName.replace(' ', '');
      const authorNames = authorNamesString.split(',');

      const authorList: Author[] = [];

      authorNames.forEach((authorName: string) => {
        authorList.push(new Author(null, authorName));
      });

      const newImageId = await this.saveNewImage(book.image?.id);

      const modifiedBook: IBook = {
        id: book.id,
        title: this.newTitle ? this.newTitle : this.orgTitle,
        isbn: this.newIsbn ? this.newIsbn.replace('-', '') : this.orgIsbn.replace('-', ''),
        authors: authorList,
        publisher: {
          name: this.newPublisherName
            ? this.newPublisherName
            : this.orgPublisherName,
        },
        image: {
          id: newImageId,
        },
      };

      const axiosResponse = await axiosInstanceWithAuth.patch('/api/book/update', modifiedBook, {
        headers: {
          'Content-Type': 'application/json',
        }
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.modifyBook(book);
        }
      });

      if(axiosResponse instanceof Result) {
        return axiosResponse as unknown as Result;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;
        const result = new Result();

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        return result;
      }
    },

    /**
     * 수정된 책 정보가 저장되기 전에 먼저 실행되는 함수.
     * 책 정보중 DB에 저장된 책 표지 정보를 새로 추가하도록 백엔드에 요청한다.
     * @async
     * @param imageId 식별을 위한 원본 이미지 고유의 ID 값
     */
    async saveNewImage(imageId?: number | null): Promise<number | null> {

      // 이미지 업데이트가 없다면, 기존 Image의 ID 값을 리턴한다
      if(!this.newImageFile) {
        return imageId ? imageId : null;
      }

      const formData = new FormData();
      formData.append('files', this.newImageFile);

      const axiosResponse = await axiosInstanceWithAuth.post('/api/image/save', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.saveNewImage(imageId);
        } else {
          return null;
        }
      });

      if(typeof axiosResponse === 'number' || axiosResponse === null) {
        return axiosResponse;
      } else {
        const response = new Response(axiosResponse as unknown as AxiosResponse)
        return response.data.imageId;
      }
    },

    /**
     * ISBN을 이용해 Naver API로 책을 검색하는 함수.
     * @async
     * @param isbn 검색할 책의 ISBN
     * @returns Result 객체
     */
    async searchByIsbn(isbn: string): Promise<Result> {
      const result = new Result();

      const axiosReponse = await axiosInstanceWithAuth.get('/api/naver-book-api/search', {
        headers: {
          'Content-Type': 'application/json',
        },
        params: { 'isbn': isbn.replaceAll('-', '') }
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.searchByIsbn(isbn);
        }
      });

      if(axiosReponse instanceof Result) {
        return axiosReponse;
      } else {
        const response = new Response(axiosReponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;
        const data = response.data;

        if(!data) {
          result.add(KeyType.SUCCESS, success);
          result.add(KeyType.MESSAGE, message);
          this.clearQinput();
          return result;
        }

        const parser = new XMLParser();
        const jsonObject = parser.parse(data);

        this.saveTitle = jsonObject.rss.channel.item.title;
        this.saveIsbn = jsonObject.rss.channel.item.isbn;
        this.saveAuthorName = jsonObject.rss.channel.item.author;
        this.savePublisherName = jsonObject.rss.channel.item.publisher;

        const imageLink = jsonObject.rss.channel.item.image;
        this.saveImageLink = imageLink;

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);
      }

      return result;
    },

    /**
     * SaveAndSearch 페이지의 저장 탭 vModel을 모두 초기화하는 함수.
     */
    clearQinput() {
      this.saveTitle = '';
      this.saveIsbn = '';
      this.saveAuthorName = '';
      this.savePublisherName = '';
      this.saveFile = null;
      this.saveImageLink = '';
    }
  },
});
