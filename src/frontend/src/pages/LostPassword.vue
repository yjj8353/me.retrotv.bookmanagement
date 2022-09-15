<template>
  <q-page-container>
    <div class="q-pa-md">
      <q-page padding>
        <div class="text-h6">패스워드 분실</div>
        <q-input ref="emailRef" v-model="email" type="email" label="가입시 사용한 이메일" @update:model-value="emailValueChange" />

        <br />

        <q-btn v-if="disableBtn"
          @click="clickSendEmail"
          class="full-width"
          color="primary"
          label="이메일 전송"
          size="24px"
          disable
          style="float: right"
        />
        <q-btn v-else
          @click="clickSendEmail"
          class="full-width"
          color="primary"
          label="이메일 전송"
          size="24px"
          style="float: right"
        />
      </q-page>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent, ref, VNodeRef } from 'vue'

import { useLoginUser } from 'src/stores/LoginUser';
import { NotifyMixin, NotifyPosition, NotifyType } from 'src/mixins/NotifyMixin';
import { useTokenStore } from 'src/stores/Token';
import { Response } from 'src/class/response';

export default defineComponent({
  name: 'LostPassword',

  mixins: [NotifyMixin],

  setup() {
    const loginUser = useLoginUser()
    const token = useTokenStore()
    const emailRef: VNodeRef = ref(null)
    const email = ''

    return {
      loginUser,
      token,
      emailRef,
      email
    }
  },

  data: () => ({
    disableBtn: false
  }),

  methods: {
    async clickSendEmail() {
      this.disableBtn = true;
      const response: Response = await this.loginUser.sendEmailPasswordChange(this.email);

      if(response.success) {
        this.notify(response.message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
        this.email = '';
        this.disableBtn = false;
        this.$router.replace('/login');
      } else {
        this.disableBtn = false;
        this.notify(response.message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
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
  }
})
</script>
