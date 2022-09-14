<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated class="glossy">
      <q-toolbar>
        <div v-if="$route.path === '/my'">
          <q-btn
            @click="logout"
            rounded
            color="negative"
            label="LOGOUT"
          />
        </div>
        <div v-else-if="$route.path === '/' && !token.existToken">
          <q-btn
            @click="login"
            rounded
            color="primary"
            label="LOGIN"
          />
        </div>
        <div v-else-if="$route.path === '/' && token.existToken">
          <q-btn
            @click="clickAdmin"
            color="primary"
            label="MY"
            rounded
          />
        </div>
        <div v-else>
          <q-btn
            @click="homeUrl"
            round
            color="primary"
            icon="home"
          />
        </div>
        <q-toolbar-title style="text-align: center;">
          도서장부
        </q-toolbar-title>
        <div>
          <q-btn
            @click="beforeUrl"
            round
            color="primary"
            icon="arrow_back_ios_new"
          />
        </div>
      </q-toolbar>
    </q-header>
    <router-view />
  </q-layout>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import { useDetailBook } from 'src/stores/DetailBook';
import { useVModelStore } from 'src/stores/VModel';
import { useTokenStore } from 'src/stores/Token';
import { useLoginUser } from 'src/stores/LoginUser';
import { NotifyMixin, NotifyPosition, NotifyType } from 'src/mixins/NotifyMixin';
import { KeyType } from 'src/class/result';

export default defineComponent({
  name: 'MainLayout',

  mixins: [NotifyMixin],

  setup: () => {
    const token = useTokenStore();
    const loginUser = useLoginUser();

    return {
      token,
      loginUser
    }
  },

  methods: {

    /**
     * Tab 위의 뒤로가기 버튼 클릭시, 발생하는 이벤트.
     * vue 라우터의 이전 히스토리로 되돌아간다.
     */
    beforeUrl() {

      // 라우트 경로가 '/'이면 더 이상 뒤로가기 버튼이 동작하지 않는다
      if (this.$route.path !== '/') {
        this.$router.back();
      }
    },

    /**
     * Tab 위의 홈 버튼 클릭시, 발생하는 이벤트.
     * uri를 root 경로로 이동하고, 일부 store 및 히스토리를 모두 초기화 한다.
     */
    homeUrl() {

      // store 값을 모조리 초기화 하고 경로를 '/'로 초기화 한다
      useVModelStore().reset();
      useDetailBook().reset();
      this.$router.replace('/');
    },

    clickAdmin() {
      this.$router.push({
        name: 'MyPage'
      })
    },

    login() {
      this.$router.push({
        name: 'LoginPage'
      });
    },

    async logout() {
      const result = await this.loginUser.logout();
      const message = result.get(KeyType.MESSAGE) as string;
      this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);

      this.homeUrl();
    }
  },
});
</script>
