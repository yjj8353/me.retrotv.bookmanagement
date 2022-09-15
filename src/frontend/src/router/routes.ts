import { useTokenStore } from 'src/stores/Token';
import { RouteRecordRaw } from 'vue-router';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const beforeAuth = (isAuth: boolean) => (_from: any, _to: any, next: any) => {
  const token = useTokenStore();
  const isAuthenticated = token.existToken;
  if ((isAuthenticated && isAuth) || (!isAuthenticated && !isAuth)) {
    return next()
  } else {
    next('/login');
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'SaveAndResult',
        component: () => import('pages/SaveAndSearch.vue'),
        beforeEnter: beforeAuth(true)
      },
      {
        path: 'my',
        name: 'MyPage',
        component: () => import('pages/MyPage.vue'),
        beforeEnter: beforeAuth(true)
      },
      {
        path: 'certify',
        name: 'CertifyPage',
        component: () => import('pages/CertifyPage.vue'),
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('pages/ChangePassword.vue'),
      },
      {
        path: 'login',
        component: () => import('layouts/MainLayout.vue'),
        children: [
          {
            path: '',
            name: 'LoginPage',
            component: () => import('pages/LoginPage.vue'),
          },
          {
            path: 'lost-password',
            name: 'LostPassword',
            component: () => import('pages/LostPassword.vue'),
          }
        ]
      },
      {
        path: 'search-result',
        component: () => import('layouts/MainLayout.vue'),
        beforeEnter: beforeAuth(true),
        children: [
          {
            path: '',
            name: 'SearchResult',
            component: () => import('pages/SearchResult.vue'),
          },
          {
            path: 'detail',
            component: () => import('layouts/MainLayout.vue'),
            children: [
              {
                path: '',
                name: 'ResultDetail',
                component: () => import('pages/ResultDetail.vue'),
              },
              {
                path: 'modify',
                name: 'ModifyBookInfo',
                component: () => import('pages/ModifyBookInfo.vue')
              },
            ],
          },
        ],
      },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
