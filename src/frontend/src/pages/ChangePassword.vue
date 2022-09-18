<template>
  <div :class="background">
    <div>
      <div class="text-h3">
        {{ text }}
      </div>

      <div v-if="!isPasswordChangeSuccess">
        <q-input v-model="newPassword" :type="isPwd1 ? 'password' : 'text'" label="새로운 패스워드" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd1 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd1 = !isPwd1"
            />
          </template>
        </q-input>
        <q-input v-model="newPasswordCheck" :type="isPwd2 ? 'password' : 'text'" label="새로운 패스워드 확인" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd2 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd2 = !isPwd2"
            />
          </template>
        </q-input>
      </div>

      <br />

      <q-btn v-if="disableBtn"
        color="white"
        @click="clickChangePassword"
        :text-color="btnTextColor"
        :label="btnLabel"
        disable
        size="20px"
      />
      <q-btn v-else
        color="white"
        @click="clickChangePassword"
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
  name: 'ChangePassword',

  setup: () => ({
    admin: useAdminStore(),
    loginUser: useLoginUser(),
    username: '',
    passcode: ''
  }),

  data:() => ({
    isPwd1: true,
    isPwd2: true,
    isPasswordChangeSuccess: false,
    background: 'fullscreen bg-blue-5 text-white text-center q-pa-md flex flex-center',
    btnLabel: '변경',
    btnTextColor: 'blue',
    disableBtn: false,
    newPassword: '',
    newPasswordCheck: '',
    text: '패스워드 변경',
  }),

  mounted() {
    this.username = this.$route.query.username as string;
    this.passcode = this.$route.query.passcode as string;
  },

  methods: {
    async clickChangePassword() {
      if(this.btnLabel === '로그인') {
        this.loginUser.logout();
        this.$router.replace('/login');
      } else {
        this.text = '변경하는 중 입니다';
        this.disableBtn = true;

        const result = await this.loginUser.passwordChange('', this.newPassword, this.newPasswordCheck, '', this.passcode);
        const success = result.get(KeyType.SUCCESS);

        if(success) {
          this.background = 'fullscreen bg-green text-white text-center q-pa-md flex flex-center';
          this.text = '변경이 완료되었습니다!';
          this.btnTextColor = 'green';
          this.btnLabel = '로그인';
          this.disableBtn = false;
          this.isPasswordChangeSuccess = true;
        } else {
          this.background = 'fullscreen bg-red text-white text-center q-pa-md flex flex-center';
          this.text = '패스워드 변경에 실패했습니다!\n다시 시도해 주세요.';
          this.btnTextColor = 'red';
          this.btnLabel = '변경';
          this.disableBtn = false;
        }
      }
    }
  }
})
</script>
