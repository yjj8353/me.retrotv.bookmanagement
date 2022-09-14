<template>
  <q-page-container>
    <div class="q-pa-md">
      <div class="text-h6">조회된 책 리스트</div>

      <!-- Object.keys(vModel.searchBooks).length 의 값이 0이면 조회된 데이터가 없다는 의미이다 -->
      <q-list
        v-if="Object.keys(vModel.searchBooks).length !== 0"
        bordered
        separator
      >
        <q-item
          v-for="(book, index) in vModel.searchBooks"
          :key="index"
          @click="clickBook(book, index)"
          clickable
          v-ripple
        >
          <q-item-section thumbnail>
            <img
              :src="vModel.imageUrls[index]"
              v-bind="getImage(book, index)"
              alt=""
            />
          </q-item-section>
          <q-item-section>
            {{ '제목: ' + book.title }}
            <br />
            {{
              book.authors
                ? book.authors.length === 1
                  ? '저자: ' + book.authors[0].name
                  : '저자: ' + book.authors[0].name + ' 외'
                : ''
            }}
          </q-item-section>
        </q-item>
      </q-list>
      <q-list v-else bordered separator>
        <q-item v-ripple>
          <q-item-section>
            {{ '조회된 책이 존재하지 않습니다.' }}
          </q-item-section>
        </q-item>
      </q-list>
      <div class="q-pa-lg flex flex-center">
        <q-pagination
          v-model="current"
          color="primary"
          :max="pageSize"
          :max-pages="5"
          boundary-numbers
        />
      </div>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import { useVModelStore } from 'src/stores/VModel';
import { IBook } from 'src/class/book';

export default defineComponent({
  name: 'SearchResult',

  data: () => ({
    vModel: useVModelStore(),
    pageSize: useVModelStore().pageSize,
    current: useVModelStore().page + 1
  }),

  watch: {
    'current': function(newValue) {
      this.vModel.page = newValue - 1;
      this.vModel.searchBooks = [] as unknown as IBook[];
      this.vModel.searchBook();
    }
  },

  methods: {

    /**
     * 특정 q-item 클릭 시, 해당 q-item 에 담긴 책 정보를 자세히 보여주기 위한 페이지로 이동하는 함수.
     * @param book 상세보기를 위한 책 정보가 담긴 Book class 객체
     * @param index 클릭한 q-item 의 순번
     */
    clickBook(book: IBook, index: number) {
      this.$router.push({
        name: 'ResultDetail',

        // params는 string 값만 전달되므로, JSON.stringfy()를 이용해 Book 객체를 JSON 문자열로 변환한다
        params: {
          book: JSON.stringify(book),
          index: index,
          imageUrl: this.vModel.imageUrls[index],
        },
      });
    },

    /**
     * 해당하는 q-item 의 책 표지 이미지를 가져오는 함수.
     * @param book 표지를 불러올 책의 정보를 담은 Book class 객체
     * @param index 해당 q-item 의 순번
     */
    async getImage(book: IBook, index: number) {
      this.vModel.downloadImage(book, index);
    }
  },
});
</script>
