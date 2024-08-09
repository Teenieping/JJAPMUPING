<template>
  <v-container>
    <v-row>
      <v-col cols="12" sm="8" md="6">
        <v-card>
          <v-card-title class="text-h5 text-center">공지사항 작성</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="submitNotice">
              <v-text-field
                label="제목"
                v-model="title"
                required
              ></v-text-field>
              <v-textarea
                label="내용"
                v-model="content"
                rows="5"
                required
              ></v-textarea>
              <v-file-input
                label="사진 업로드"
                v-model="photo"
                accept="image/*"
              ></v-file-input>
              <v-btn block type="submit" color="primary">공지사항 작성</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from 'axios';

export default {
  name: "CreateNoticePage",
  data() {
    return {
      title: "",
      content: "",
      photo: null,
    };
  },
  methods: {
    async submitNotice() {
      const formData = new FormData();
      formData.append('title', this.title);
      formData.append('content', this.content);
      if (this.photo) {
        formData.append('photo', this.photo);
      }

      try {
        const response = await axios.post(`${process.env.VUE_APP_API_BASE_URL}/notice/create`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
        alert('공지사항이 성공적으로 작성되었습니다.');
        this.$router.push('/notices'); // 공지사항 리스트 페이지로 이동
      } catch (e) {
        console.error('공지사항 작성에 실패했습니다.', e);
        alert('공지사항 작성에 실패했습니다.');
      }
    }
  }
};
</script>

<style scoped>
/* 스타일은 필요에 따라 조정하세요 */
</style>
