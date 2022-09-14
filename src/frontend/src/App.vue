<template>
  <router-view />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTokenStore } from './stores/Token';

export default defineComponent({
  name: 'App',

  setup: () => {
    const token = useTokenStore();
    token.getRefreshTokenFromCookies();
    token.checkTokenValid();

    return {
      token
    }
  },

  mounted() {
    if(this.token.refreshToken) {
      this.$router.replace('/login');
    }
  }
});
</script>
