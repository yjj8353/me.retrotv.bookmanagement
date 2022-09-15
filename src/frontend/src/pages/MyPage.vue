<template>
  <q-page-container>
    <div class="q-pa-md">
      <q-page padding>
        <div class="text-h6">패스워드 변경</div>
        <q-input v-model="orgPassword" :type="isPwd1 ? 'password' : 'text'" label="기존 패스워드" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd1 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd1 = !isPwd1"
            />
          </template>
        </q-input>
        <q-input v-model="newPassword" :type="isPwd2 ? 'password' : 'text'" label="새로운 패스워드" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd2 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd2 = !isPwd2"
            />
          </template>
        </q-input>
        <q-input v-model="newPasswordCheck" :type="isPwd3 ? 'password' : 'text'" label="새로운 패스워드 확인" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd3 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd3 = !isPwd3"
            />
          </template>
        </q-input>

        <br />

        <q-btn
          @click="clickPasswordChange"
          class="full-width"
          color="primary"
          label="패스워드 변경"
          size="24px"
          style="float: right"
        />
      </q-page>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

import { useLoginUser } from 'src/stores/LoginUser';
import { NotifyMixin, NotifyPosition, NotifyType } from 'src/mixins/NotifyMixin';
import { KeyType } from 'src/class/result';
import { useTokenStore } from 'src/stores/Token';

export default defineComponent({
  name: 'MyPage',

  mixins: [NotifyMixin],

  setup: () => ({
    loginUser: useLoginUser(),
    token: useTokenStore()
  }),

  data: () => ({
    id: '',
    orgPassword: '',
    newPassword: '',
    newPasswordCheck: '',
    isPwd1: true,
    isPwd2: true,
    isPwd3: true
  }),

  methods: {
    async clickPasswordChange() {
      const result = await this.loginUser.passwordChange(this.orgPassword, this.newPassword, this.newPasswordCheck, this.token.getRefreshToken, '');
      if(result.get(KeyType.SUCCESS)) {
        this.notify(result.get(KeyType.MESSAGE) as string, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
        this.orgPassword = '';
        this.newPassword = '';
        this.newPasswordCheck = '';
        this.loginUser.logout();
        this.$router.replace('/login');
      } else {
        this.notify(result.get(KeyType.MESSAGE) as string, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }
    }
  }
})
</script>
