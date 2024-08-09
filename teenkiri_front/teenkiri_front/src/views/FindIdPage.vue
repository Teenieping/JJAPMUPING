<template>
    <v-container>
      <v-row justify="center">
        <v-col cols="12" sm="6" md="8">
          <v-card>
            <v-card-title class="text-h5 text-center">이메일 찾기</v-card-title>
            <v-card-text>
              <v-form @submit.prevent="findEmail">
                <v-text-field
                  label="이름"
                  v-model="name"
                  required
                  prepend-icon="mdi-account"
                ></v-text-field>
                <v-text-field
                  label="전화번호"
                  v-model="phone"
                  type="tel"
                  required
                  prepend-icon="mdi-phone"
                ></v-text-field>
                <v-btn block type="submit" color="primary">이메일 찾기</v-btn>
                <v-divider class="my-4"></v-divider>
                <v-btn block @click="$router.push('/login')">로그인 페이지로 돌아가기</v-btn>
              </v-form>
              <v-alert
                v-if="userEmail"
                type="success"
                class="mt-4"
                border="left"
              >
                찾으신 이메일 주소는: {{ userEmail }}
              </v-alert>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'FindIdPage',
    data() {
      return {
        name: '',
        phone: '',
        userEmail: '', // 이메일 주소를 저장할 데이터
      };
    },
    methods: {
      async findEmail() {
        try {
          const body = {
            name: this.name,
            phone: this.phone
          };
          const response = await axios.post(`${process.env.VUE_APP_API_BASE_URL}/user/find-id`, body);
  
          // 서버 응답에서 이메일을 추출
          this.userEmail = response.data; // 마스킹된 이메일을 저장
          alert('이메일 찾기 요청이 완료되었습니다. 결과를 확인해 주세요.');
          console.log(response.data);
        } catch (e) {
          // 오류 메시지 처리
          let error_message = '오류가 발생했습니다.';
  
          // 오류 객체 구조를 안전하게 확인
          if (e.response && e.response.data) {
            error_message = e.response.data.status_message || '서버에서 오류가 발생했습니다.';
          } else if (e.message) {
            error_message = e.message;
          }
  
          alert('이메일 찾기 실패: ' + error_message);
          console.error('Error details:', e);
        }
      },
    },
  };
  </script>
  