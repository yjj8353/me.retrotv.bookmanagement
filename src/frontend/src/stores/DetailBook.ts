import { defineStore } from 'pinia';
import { useVModelStore } from './VModel';
import { IBook } from 'src/class/book';
import { KeyType, Result } from 'src/class/result';
import { axiosInstanceWithAuth } from 'src/util/AxiosInstance';
import { AxiosResponse } from 'axios';
import { Response } from 'src/class/response';

export interface DetailBook {
  book: IBook;
  imageUrl: string;
}

export const useDetailBook = defineStore('detailBook', {
  state: () => ({
    vModel: useVModelStore(),
    book: null as unknown as IBook,
    imageUrl: '',
  }),

  getters: {},

  actions: {

    /**
     * DetailBook store를 초기화 하는 함수.
     */
    reset() {
      this.$reset();
    },

    /**
     * 현재 ResultDetail 페이지에 표시된 책을 삭제한다.
     * @async
     * @param bookId DB에 저장된 Book 객체를 식별하기 위한 고유의 ID 값
     */
    async deleteBook(bookId: number): Promise<Result> {
      const axiosReponse = await axiosInstanceWithAuth.delete('/api/book/delete', {
        data: {
          id: bookId,
        },
      }).catch((error) => {
        if(error.response.status === 417) {
          return this.deleteBook(bookId);
        }
      });

      if(axiosReponse instanceof Result) {
        return axiosReponse as unknown as Result;
      } else {
        const response = new Response(axiosReponse as unknown as AxiosResponse);
        const success = response.success;
        const message = response.message;
        const result = new Result();

        result.add(KeyType.SUCCESS, success);
        result.add(KeyType.MESSAGE, message);

        if(success) {

          // 관련 없는 리스트 부터 삭제되므로 체크
          this.vModel.searchBooks.forEach((book, index) => {
            if(book.id === bookId) {
              this.vModel.searchBooks.splice(index, 1);
              this.vModel.imageUrls.splice(index, 1);
              return true;
            }
          });
        }

        return result;
      }
    }
  },
});
