
import NoticeCreate from "@/views/NoticeCreate.vue";
import NoticeList from "@/views/NoticeList.vue";
import NoticeDetail from "@/views/NoticeDetail.vue";

export const memberRouter = [
    {
        path: '/notice/create',
        name: 'NoticeCreate',
        component: NoticeCreate
    },
    {
        path: '/notice/list',
        name: 'NoticeList',
        component: NoticeList
    },
    {
        path: '/notice/detail',
        name: 'NoticeDetail',
        component: NoticeDetail
    },
]