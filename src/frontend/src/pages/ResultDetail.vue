<template>
  <q-page-container>
    <div class="q-pa-md items-start q-gutter-md">
      <div>
        <div class="text-h6">책 정보</div>
        <q-img
          :src="imageUrl"
          style="max-width: 50%; display: block; margin: 0px auto"
        ></q-img>
      </div>

      <br />

      <div>
        <q-table
          title="책 정보"
          :rows="rows"
          :columns="columns"
          row-key="name"
          :pagination="pagination"
          :table-header-style="{ height: '0px' }"
        >
          <template v-slot:top>
            <div style="width: 50%; float: left">
              <q-btn
                color="warning"
                label="수정"
                size="20px"
                style="margin: 0 4px 0 0; width: 100%"
                @click="modifyThisBook(book, imageUrl)"
              />
            </div>
            <div style="width: 50%; float: right">
              <q-btn
                color="negative"
                label="삭제"
                size="20px"
                style="margin: 0 0 0 4px; width: 100%"
                @click="deleteThisBook(bookId)"
              />
            </div>
          </template>
        </q-table>
      </div>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useDetailBook } from 'src/stores/DetailBook';
import { useVModelStore } from 'src/stores/VModel';

import { QTableProps } from 'quasar';
import { IAuthor } from 'src/class/author';
import { IBook } from 'src/class/book';
import {
  NotifyMixin,
  NotifyType,
  NotifyPosition,
} from 'src/mixins/NotifyMixin';
import { KeyType } from 'src/class/result';

export default defineComponent({
  name: 'ResultDetail',

  mixins: [NotifyMixin],

  setup: () => {

    // setup 에서는 아직 this.$route가 세팅되어있지 않으므로 useRoute()를 써야한다.
    const route = useRoute();
    const vModel = useVModelStore();
    const detailBook = useDetailBook();
    let book = null;
    let index = null;
    let imageUrl = null;

    // SearchResult 컴포넌트에서 params 값으로 넘어온 값이 없는 경우,
    // 뒤로가기를 통해 이동한 경우 이므로 store의 params을 사용한다.
    if(!route.params.book) {
      book = detailBook.book;
      imageUrl = detailBook.imageUrl;
    } else {
      book = JSON.parse(route.params.book as string);
      index = route.params.index as unknown as number;
      imageUrl = vModel.imageUrls[index];

      detailBook.book = JSON.parse(route.params.book as string) as IBook;
      detailBook.imageUrl = route.params.imageUrl as string;
    }

    let authors = '';

    book.authors.forEach((author: IAuthor, i: number) => {
      if (i !== 0) {
        authors = authors + ', ' + author.name;
      } else {
        authors = authors + author.name;
      }
    });

    return {
      detailBook: useDetailBook(),
      book: book,
      columns: [
        {
          name: 'column',
          label: '',
          required: true,
          align: 'left',

          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          field: (row: any) => row.column,

          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          format: (val: any) => `${val}`,
          style: 'width: 20%',
        },
        { name: 'data', align: 'left', field: 'data' },
      ] as QTableProps['columns'],

      rows: [
        { column: '제목', data: book.title },
        { column: 'ISBN', data: book.isbn },
        { column: '저자', data: authors },
        { column: '출판사', data: book.publisher.name },
      ],

      pagination: ref({
        rowsPerPage: 0,
      }),

      imageUrl: imageUrl,
      bookId: book.id,
    };
  },

  data: () => ({
    vModel: useVModelStore(),
  }),

  methods: {

    /**
     * 수정 버튼 클릭시 실행되는 함수.
     * 현재 상세 표시된 책을 수정하기 위한 페이지로 이동한다.
     * @param book 현재 상세 표시된 책 정보가 담긴 Book class 객체.
     * @param imageUrl 현재 상세 표시된 책 정보중, 이미지의 url.
     */
    modifyThisBook(book: IBook, imageUrl: string) {
      this.$router.push({
        name: 'ModifyBookInfo',

        // JSON 데이터는 반드시 stringfy로 넘겨야 한다
        params: { book: JSON.stringify(book), imageUrl: imageUrl },
      });
    },

    /**
     * 삭제 버튼 클릭시 실행되는 함수.
     * 현재 상세 표시된 책을 삭제한다.
     * @async
     * @param bookId DB에 저장된 Book 객체를 식별하기 위한 고유의 ID 값
     */
    async deleteThisBook(bookId: number) {
      const result = await this.detailBook.deleteBook(bookId);

      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
      } else {
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }

      this.$router.back();
    },
  },
});
</script>
