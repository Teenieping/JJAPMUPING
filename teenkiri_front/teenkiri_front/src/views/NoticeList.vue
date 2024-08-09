<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="text-h5 text-center">공지사항</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item-group v-if="notices.length > 0" v-for="notice in notices" :key="notice.id" @click="goToNotice(notice.id)">
                <v-list-item>
                  <v-list-item-content>
                    <v-list-item-title>{{ notice.title }}</v-list-item-title>
                    <v-list-item-subtitle>{{ formatDate(notice.date) }}</v-list-item-subtitle>
                  </v-list-item-content>
                </v-list-item>
              </v-list-item-group>
              <v-list-item v-else>
                <v-list-item-content>
                  <v-list-item-title>공지사항이 없습니다.</v-list-item-title>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from 'axios';

export default {
  name: "NoticeListPage",
  data() {
    return {
      notices: [],
    };
  },
  async created() {
    await this.fetchNotices();
  },
  methods: {
    async fetchNotices() {
      try {
        const response = await axios.get(`${process.env.VUE_APP_API_BASE_URL}/notice/list`);
        this.notices = response.data.notices;
      } catch (e) {
        console.error('공지사항을 가져오는 데 실패했습니다.', e);
        // 에러 처리 추가 가능
      }
    },
    goToNotice(noticeId) {
      this.$router.push(`/notices/${noticeId}`);
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString(); // 필요한 형식으로 포맷을 조정
    }
  },
};
</script>

<style scoped>
/* 스타일은 필요에 따라 조정하세요 */
</style>
