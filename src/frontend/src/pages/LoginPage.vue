<template>
  <q-page-container>
    <div class="q-pa-md">
      <q-page padding>
        <div class="text-h6">{{ title }}</div>
        <q-input v-model="id" ref="idRef" label="아이디" maxlength="20" @update:model-value="idValueChange" />
        <q-input v-model="password" ref="passwordRef" :type="isPwd1 ? 'password' : 'text'" label="패스워드" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd1 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd1 = !isPwd1"
            />
          </template>
        </q-input>
        <q-input v-if="joinMode" v-model="passwordCheck" :type="isPwd2 ? 'password' : 'text'" label="패스워드 확인" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd2 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd2 = !isPwd2"
            />
          </template>
        </q-input>
        <q-input v-if="joinMode" v-model="realName" ref="realNameRef" label="이름" maxlength="20" @update:model-value="realNameValueChange" />
        <q-input v-if="joinMode" ref="emailRef" v-model="email" type="email" label="이메일" @update:model-value="emailValueChange" />
        <br />
        <div class="row">
          <div v-if="btnDisable1" class="col">
            <q-btn
              @click="clickBtn1"
              :color="btn1Color"
              :label="btn1Label"
              disable
              size="20px"
              style="margin: 0 2% 0 0; width: 98%"
            />
          </div>
          <div v-else class="col">
            <q-btn
              @click="clickBtn1"
              :color="btn1Color"
              :label="btn1Label"
              size="20px"
              style="margin: 0 2% 0 0; width: 98%"
            />
          </div>
          <div class="col">
            <q-btn
              @click="clickBtn2"
              :color="btn2Color"
              :label="btn2Label"
              size="20px"
              style="margin: 0 0 0 2%; width: 98%"
            />
          </div>
        </div>
        <div class="row">
          <div v-if="!btnDisable2" class="col">
            <q-btn
              @click="clickChangePassword"
              color="warning"
              label="패스워드 분실"
              size="20px"
              style="margin: 5px 0 0 0; width: 100%"
            />
          </div>
        </div>
      </q-page>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent, ref, VNodeRef } from 'vue';

import { NotifyMixin, NotifyPosition, NotifyType } from 'src/mixins/NotifyMixin';
import { useLoginUser } from 'src/stores/LoginUser';
import { KeyType } from 'src/class/result';

export default defineComponent({
  name: 'LoginPage',

  mixins: [NotifyMixin],

  setup() {
    const idRef: VNodeRef = ref(null);
    const passwordRef: VNodeRef = ref(null);
    const realNameRef: VNodeRef = ref(null);
    const emailRef: VNodeRef = ref(null);
    return {
      idRef,
      passwordRef,
      realNameRef,
      emailRef,
      loginUser: useLoginUser()
    };
  },

  data: () => ({
    id: '',
    isPwd1: true,
    isPwd2: true,
    password: '',
    passwordCheck: '',
    realName: '',
    email: '',
    joinMode: false,
    btnDisable1: false,
    btnDisable2: false,
    title: '로그인',
    btn1Label: '로그인',
    btn2Label: '회원가입',
    btn1Color: 'positive',
    btn2Color: 'primary'
  }),

  methods: {

    /**
     * 로그인/가입요청 버튼 클릭시 실행되는 함수.
     * @async
     */
    async clickBtn1() {
      if(this.joinMode) {
        this.btnDisable1 = true;
        this.join();
      } else {
        const result = await this.loginUser.login(this.id, this.password);

        if(result.get(KeyType.SUCCESS)) {
          this.$router.replace('/');
        } else {
          this.notify(result.get(KeyType.MESSAGE) as string, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
        }
      }
    },

    /**
     * 관리자추가/취소 버튼 클릭시 실행되는 함수.
     */
    clickBtn2() {
      if(!this.joinMode) {
        this.joinMode = true;
        this.id = '';
        this.password = '';
        this.passwordCheck = '';
        this.isPwd1 = true;
        this.isPwd2 = true;
        this.realName = '';
        this.email = '';
        this.title = '가입';
        this.btn1Label = '가입요청'
        this.btn2Label = '취소';
        this.btn2Color = 'negative';
        this.btnDisable2 = true;
      } else {
        this.joinMode = false;
        this.id = '';
        this.password = '';
        this.passwordCheck = '';
        this.isPwd1 = true;
        this.isPwd2 = true;
        this.realName = '';
        this.email = '';
        this.title = '로그인';
        this.btn1Label = '로그인'
        this.btn2Label = '회원가입';
        this.btn2Color = 'primary';
        this.btnDisable2 = false;
      }
    },

    /**
     * q-input의 id 값이 변경될 때, 발생하는 이벤트 함수.
     */
    idValueChange() {
      const regex = /[^a-zA-Z0-9]/g;
      if(regex.exec(this.id) !== null) {
        let value: string = this.id;
        this.id = value.replace(regex, '');
        this.idRef.blur();
        this.idRef.focus();
      }
    },

    /**
     * q-input의 realName 값이 변경될 때, 발생하는 이벤트 함수.
     */
    realNameValueChange() {
      let regex = /[^a-zA-Z0-9가-힣\.\s]/g;
      if(regex.exec(this.realName) !== null) {
        let value: string = this.realName;
        this.realName = value.replace(regex, '');
        this.realNameRef.blur();
        this.realNameRef.focus();
      }

      // 2칸 이상 공백 금지
      regex = /\s\s*/g;
      if(regex.exec(this.realName) !== null) {
        let value: string = this.realName;
        this.realName = value.replace(regex, ' ');
        this.realNameRef.blur();
        this.realNameRef.focus();
      }
    },

    /**
     * q-input의 email 값이 변경될 때, 발생하는 이벤트 함수.
     */
    emailValueChange() {
      const regex = /[^a-zA-Z0-9\.@]/g;
      if(regex.exec(this.email) !== null) {
        let value: string = this.email;
        this.email = value.replace(regex, '');
        this.emailRef.blur();
        this.emailRef.focus();
      }
    },

    clickChangePassword() {
      this.$router.push({
        name: 'LostPassword'
      })
    },

    /**
     * 회원가입 처리 함수.
     */
    async join() {
      if(this.password !== this.passwordCheck) {
        this.password = '';
        this.passwordCheck = '';
        this.passwordRef.focus();
        this.notify('패스워드가 서로 다릅니다.\n패스워드를 확인해 주세요.', NotifyType.NEGATIVE, NotifyPosition.BOTTOM);

        return;
      }

      const result = await this.loginUser.join(this.id, this.password, this.passwordCheck, this.realName, this.email);
      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.id = '';
        this.password = '';
        this.passwordCheck = '';
        this.isPwd1 = true;
        this.isPwd2 = true;
        this.realName = '';
        this.email = '';

        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
        this.clickBtn2();
      } else {
        if(message.includes('계정')) { this.idRef.focus(); }
        if(message.includes('이메일')) { this.emailRef.focus(); }
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }

      this.btnDisable1 = false;
    }
  }
});
</script>
