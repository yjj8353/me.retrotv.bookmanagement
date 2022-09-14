<template>
  <q-page-container>
    <q-tabs
      v-model="tab"
      class="bg-primary text-white shadow-2"
      align="justify"
    >
      <q-tab name="save" label="저장" />
      <q-tab name="search" label="조회" />
    </q-tabs>

    <q-separator />

    <q-page padding>
      <q-tab-panels v-model="tab" animated>
        <q-tab-panel name="save">
          <div v-if="token.existToken">
            <div class="text-h6">새로운 책 추가</div>

            <div class="row">
              <div class="col">
                <q-btn
                  label="ISBN으로 검색"
                  color="primary"
                  size="20px"
                  style="margin: 0 4px 0 0; width: 100%"
                  @click="isbnSearch = true"
                />
              </div>
              <div class="col">
                <q-btn
                  label="입력칸 초기화"
                  color="warning"
                  size="20px"
                  style="margin: 0 0 0 4px; width: 100%"
                  @click="vModel.clearQinput"
                />
              </div>
            </div>
            <q-dialog v-model="isbnSearch" persistent>
              <q-card style="min-width: 350px">
                <q-card-section>
                  <div class="text-h6">ISBN</div>
                </q-card-section>

                <q-card-section class="q-pt-none">
                  <q-input dense v-model="isbn" mask="###-##-####-###-#" hint="ISBN은 바코드 밑의 13자리 숫자 입니다" autofocus @keyup.enter="isbnSearch = false" />
                </q-card-section>

                <q-card-actions align="right" class="text-primary">
                  <q-btn flat label="취소" v-close-popup />
                  <q-btn flat label="조회" @click="clickSearchByIsbn" />
                </q-card-actions>
              </q-card>
            </q-dialog>

            <q-input v-model="vModel.saveTitle" label="제목" hint="(필수)" />
            <q-input v-model="vModel.saveIsbn" label="ISBN" mask="###-##-####-###-#" hint="ISBN은 바코드 밑의 13자리 숫자 입니다" />
            <q-input v-model="vModel.saveAuthorName" label="저자" hint="저자가 여러명일 경우 쉼표(,)로 구분하세요" @update:model-value="authorValueChange" />
            <q-input v-model="vModel.savePublisherName" label="출판사" @update:model-value="publisherValueChange" />

            <br />

            <q-file v-if="vModel.saveImageLink"
              v-model="vModel.saveFile"
              label="책 이미지 (첨부 됨)"
              disable
              square
              flat
              counter
              outlined
              use-chips
              clearable
              accept=".jpg,.png,.gif,.jpeg,.webp"
              max-files="1"
              max-file-size="5120000"
              hint="링크를 통해 첨부되었습니다"
              @rejected="onRejected"
            >
              <template v-slot:prepend>
                <q-icon name="attach_file" />
              </template>
            </q-file>

            <q-file v-else
              v-model="vModel.saveFile"
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
              hint="최대 5MB까지 첨부 가능합니다"
              @rejected="onRejected"
            >
              <template v-slot:prepend>
                <q-icon name="attach_file" />
              </template>
            </q-file>

            <br />

            <q-btn
              @click="clickSave"
              class="full-width"
              color="primary"
              label="저장"
              size="24px"
              style="float: right"
            />
          </div>
          <div v-else>
            <q-item-section>
              {{ "로그인이 필요한 서비스 입니다." }}
            </q-item-section>
          </div>
        </q-tab-panel>

        <q-tab-panel name="search">
          <div class="text-h6">책 조회</div>
          <q-input v-model="vModel.searchTitle" label="제목" />
          <q-input v-model="vModel.searchAuthorName" label="저자" />
          <q-input v-model="vModel.searchPublisherName" label="출판사" />
          <br />
          <q-btn
            @click="clickSearch"
            class="full-width"
            color="primary"
            label="조회"
            size="24px"
            style="float: right"
          />
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

import { useVModelStore } from 'src/stores/VModel';

import { QRejectedEntry } from 'quasar';
import { KeyType } from 'src/class/result';
import { NotifyMixin, NotifyType, NotifyPosition } from 'src/mixins/NotifyMixin';
import { useTokenStore } from 'src/stores/Token';

export default defineComponent({
  name: 'SaveAndResult',

  mixins: [NotifyMixin],

  setup: () => ({
    isbn: '',
    isbnSearch: ref(false),
    tab: ref('search'),
    vModel: useVModelStore(),
    token: useTokenStore()
  }),

  mounted() {
    this.vModel.reset();
  },

  methods: {

    /**
     * 저장 버튼 클릭시 실행되는 함수.
     * 해당 함수 실행 시, 저장 탭에 있는 책 정보가 백엔드를 통해 저장되고 저장 결과를 Quasar notify 기능으로 알림.
     * @async
     */
    async clickSave() {
      let imageId = null;
      if(this.vModel.saveFile) {
        imageId = await this.vModel.saveImage();
      } else {
        imageId = await this.vModel.saveImageByUrl();
      }
      const result = await this.vModel.saveBook(imageId);

      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
      } else {
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }
    },

    /**
     * 조회 버튼 클릭시 실행되는 함수.
     * 해당 함수 실행 시, 조회 탭에 있는 책 정보를 바탕으로 백엔드에 요청하고 SearchResult 페이지로 이동함.
     * @async
     */
    async clickSearch() {
      const result = await this.vModel.searchBook();

      if(result) {
        this.$router.push({
          name: 'SearchResult',
        });
      }
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

    /**
     * ISBN으로 검색 버튼 클릭 시, 실행되는 함수.
     * @async
     */
    async clickSearchByIsbn() {
      const result = await this.vModel.searchByIsbn(this.isbn);
      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.isbn = '';
        this.isbnSearch = false;
        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
      } else {
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }
    },

    /**
     * 입력칸 초기화 버튼 클릭 시, 실행되는 함수.
     */
    removeFileAndImageUrl() {
      this.vModel.saveFile = null;
      this.vModel.saveImageLink = '';
    }
  },
});
</script>
