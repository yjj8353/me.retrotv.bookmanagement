<template>
  <q-page-container>
    <div class="q-pa-md">
      <div class="text-h6">책 정보 수정</div>

      <q-input v-model="vModel.newTitle" :placeholder="vModel.orgTitle" hint="(필수)" />
      <q-input v-model="vModel.newIsbn" :placeholder="vModel.orgIsbn" mask="###-#-##-######-#" hint="ISBN은 바코드 밑의 13자리 숫자 입니다" />
      <q-input v-model="vModel.newAuthorName" :placeholder="vModel.orgAuthorName" hint="저자가 여러명일 경우 쉼표(,)로 구분하세요" @update:model-value="authorValueChange" />
      <q-input v-model="vModel.newPublisherName" :placeholder="vModel.orgPublisherName" @update:model-value="publisherValueChange" />

      <br />

      <q-img
        :src="vModel.orgImageUrl"
        style="max-width: 50%; display: block; margin: 0px auto"
      ></q-img>

      <br />

      <q-file
        v-model="vModel.newImageFile"
        label="책 이미지"
        square
        flat
        counter
        outlined
        use-chips
        clearable
        accept=".jpg,.png,.gif,.jpeg,.webp"
        max-files="1"
        max-file-size="5120000"
        @rejected="onRejected"
      >
        <template v-slot:prepend>
          <q-icon name="attach_file" />
        </template>
      </q-file>

      <br />

      <div class="row">
        <div class="col">
          <q-btn
            color="warning"
            label="수정"
            size="20px"
            style="margin: 0 4px 0 0; width: 100%"
            @click="modifyThisBook"
          />
        </div>
        <div class="col">
          <q-btn
            color="negative"
            label="취소"
            size="20px"
            style="margin: 0 0 0 4px; width: 100%"
            @click="modifyCancle"
          />
        </div>
      </div>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useRoute } from 'vue-router';

import { QRejectedEntry } from 'quasar';
import { IAuthor } from 'src/class/author';
import { Book, IBook } from 'src/class/book';
import { NotifyMixin, NotifyType, NotifyPosition, } from 'src/mixins/NotifyMixin';
import { useVModelStore } from 'src/stores/VModel';
import { KeyType } from 'src/class/result';

export default defineComponent({
  name: 'ModifyBookInfo',

  mixins: [NotifyMixin],

  setup: () => {
    const route = useRoute();
    const vModel = useVModelStore();
    const book = JSON.parse(route.params.book as string) as IBook;
    let authors = '';

    book.authors?.forEach((author: IAuthor, index: number) => {
      if(index !== 0) {
        authors = authors + ', ' + author.name;
      } else {
        authors = authors + author.name;
      }
    });

    const imageUrl = route.params.imageUrl;

    vModel.orgTitle = book.title ? book.title : '';
    vModel.orgIsbn = book.isbn ? book.isbn : '';
    vModel.orgAuthorName = authors;
    vModel.orgPublisherName = book.publisher?.name ? book.publisher.name : '';
    vModel.orgImageUrl = imageUrl ? imageUrl as string : '';

    return {
      vModel: useVModelStore(),
      book: book,
      imageUrl: imageUrl as string,
    };
  },

  methods: {

    /**
     * 취소 버튼 클릭시 실행되는 함수.
     * 수정사항 적용 없이, 이전 페이지로 이동한다.
     */
    modifyCancle() {
      this.$router.back();
    },

    /**
     * 수정 버튼 클릭시 실행되는 함수.
     * 해당 화면에 작성된 내용을 기반으로 DB에 덮어쓰도록 백엔드에 요청한다.
     * @async
     */
    async modifyThisBook() {
      const result = await this.vModel.modifyBook(this.book as Book);
      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
      } else {
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }

      this.$router.replace('/');
    },

    /**
     * q-file 컴포넌트에서 rejected 이벤트가 발생할 경우 실행되는 함수.
     * @param entries q-file 컴포넌트에서 파일 첨부 시 rejected 이벤트가 발생할 경우, 해당 rejected 이벤트에 대한 정보를 담고 있는 객체
     */
    onRejected(entries: QRejectedEntry[]) {
      if(entries.length > 0) {
        if(entries[0].failedPropValidation === 'max-file-size') {
          this.notify(
            '파일 용량은 5MB를 초과할 수 없습니다.',
            NotifyType.POSITIVE,
            NotifyPosition.BOTTOM
          );
        }
      }
    },

    /**
     * 저자 정보가 변경될 때 발생하는 이벤트 함수.
     */
    authorValueChange() {

      // 영문자, 한글, 마침표, 쉼표를 제외한 문자 금지.
      let regex = /[^a-zA-Zㄱ-ㅎ가-힣\.,\s]/g;
      const value = this.vModel.saveAuthorName;
      if(regex.exec(value) !== null) {
        this.vModel.saveAuthorName = value.replace(regex, '');
      }

      // 2칸 이상 공백 금지
      regex = /\s\s*/g;
      if(regex.exec(value) !== null) {
        this.vModel.saveAuthorName = value.replace(regex, ' ');
      }
    },

    /**
     * 출판사 정보가 변경될 때 발생하는 이벤트 함수.
     */
    publisherValueChange() {

      // 영문자, 한글, 마침표를 제외한 문자 금지.
      let regex = /[^a-zA-Zㄱ-ㅎ가-힣\.\s]/g;
      const value = this.vModel.savePublisherName;
      if(regex.exec(value) !== null) {
        this.vModel.savePublisherName = value.replace(regex, '');
      }

      // 2칸 이상 공백 금지
      regex = /\s\s*/g;
      if(regex.exec(value) !== null) {
        this.vModel.savePublisherName = value.replace(regex, ' ');
      }
    },
  },
});
</script>
