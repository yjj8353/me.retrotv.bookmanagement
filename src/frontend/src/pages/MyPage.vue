<template>
  <q-page-container>
    <div class="q-pa-md">
      <q-page padding>
        <q-input v-model="password" :type="isPwd1 ? 'password' : 'text'" label="패스워드" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd1 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd1 = !isPwd1"
            />
          </template>
        </q-input>
        <q-input v-model="passwordCheck" :type="isPwd2 ? 'password' : 'text'" label="패스워드 확인" maxlength="20">
          <template v-slot:append>
            <q-icon
              :name="isPwd2 ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd2 = !isPwd2"
            />
          </template>
        </q-input>
      </q-page>
    </div>
  </q-page-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'MyPage',

  data: () => ({
    id: '',
    password: '',
    passwordCheck: '',
    isPwd1: true,
    isPwd2: true
  }),

  methods: {

  }
})
</script>
