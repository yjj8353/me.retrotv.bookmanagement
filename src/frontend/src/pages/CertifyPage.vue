<template>
  <div :class="background">
    <div>
      <div style="font-size: 12vh">
        {{ title }}
      </div>

      <div class="text-h3" style="opacity:.4">
        {{ text }}
      </div>
      <br />

      <q-btn v-if="disableBtn"
        color="white"
        @click="clickCertify"
        :text-color="btnTextColor"
        :label="btnLabel"
        disable
        size="20px"
      />
      <q-btn v-else
        color="white"
        @click="clickCertify"
        :text-color="btnTextColor"
        :label="btnLabel"
        size="20px"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { KeyType } from 'src/class/result';
import { useAdminStore } from 'src/stores/Admin'
import { useLoginUser } from 'src/stores/LoginUser';
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'CertifyPage',

  setup: () => ({
    admin: useAdminStore(),
    loginUser: useLoginUser(),
    username: '',
    passcode: ''
  }),

  data:() => ({
    background: 'fullscreen bg-blue text-white text-center q-pa-md flex flex-center',
    btnLabel: '인증',
    btnTextColor: 'blue',
    disableBtn: false,
    title: '확인',
    text: '인증 버튼을 클릭해 주세요',
  }),

  mounted() {
    this.username = this.$route.query.username as string;
    this.passcode = this.$route.query.passcode as string;
  },

  methods: {
    async clickCertify() {
      if(this.btnLabel === '로그인') {
        this.loginUser.logout();
        this.$router.replace('/login');
      } else {
        this.title = '인증 중...';
        this.text = '인증하는 중 입니다';
        this.disableBtn = true;

        const result = await this.admin.certifyMember(this.username, this.passcode);
        const success = result.get(KeyType.SUCCESS);

        if(success) {
          this.background = 'fullscreen bg-green text-white text-center q-pa-md flex flex-center';
          this.title = '완료!';
          this.text = '인증이 완료되었습니다!';
          this.btnTextColor = 'green';
          this.btnLabel = '로그인';
          this.disableBtn = false;
        } else {
          this.background = 'fullscreen bg-red text-white text-center q-pa-md flex flex-center';
          this.title = '실패!';
          this.text = '인증을 다시 시도해 주세요';
          this.btnTextColor = 'red';
          this.btnLabel = '인증';
          this.disableBtn = false;
        }
      }
    }
  }
})
</script>
