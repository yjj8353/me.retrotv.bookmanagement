<template>
  <q-page-container>
    <q-page padding>
      <q-list
        v-if="admin.members && admin.members.length !== 0"
        bordered
        separator
      >
        <q-item
          v-for="(member, index) in admin.members"
          :key="index"
          v-ripple
        >
          <div style="width: 100%;">
            <div style="float: left">
              {{ member.name }}
            </div>
            <div style="float: right">
              <q-btn
                @click="certify(member.name as string)"
                rounded
                color="positive"
                label="인증"
              />
            </div>
          </div>
        </q-item>
      </q-list>
      <q-list v-else bordered separator>
        {{ '가입요청이 들어온 계정이 존재하지 않습니다.' }}
      </q-list>
    </q-page>
  </q-page-container>
</template>

<script lang="ts">
import { useAdminStore } from 'src/stores/Admin';
import { NotifyMixin, NotifyPosition, NotifyType } from 'src/mixins/NotifyMixin';
import { defineComponent } from 'vue';
import { useLoginUser } from 'src/stores/LoginUser';
import { KeyType } from 'src/class/result';

export default defineComponent({
  name: 'AdminPage',

  mixins: [NotifyMixin],

  setup() {
    const admin = useAdminStore();
    admin.getNotCertifiedMemebers();

    return {
      admin: useAdminStore(),
      loginUser: useLoginUser()
    }
  },

  methods: {

    /**
     * 인증 버튼을 클릭한 계정을 인증 하는 함수.
     * @async
     * @param id 계정
     */
    async certify(id: string) {
      const result = await this.admin.certifyMember(id, '');

      const success = result.get(KeyType.SUCCESS);
      const message = result.get(KeyType.MESSAGE) as string;

      if(success) {
        this.notify(message, NotifyType.POSITIVE, NotifyPosition.BOTTOM);
      } else {
        this.notify(message, NotifyType.NEGATIVE, NotifyPosition.BOTTOM);
      }
    }
  }
})
</script>
