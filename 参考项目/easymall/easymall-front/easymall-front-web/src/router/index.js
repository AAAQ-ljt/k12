import { createRouter, createWebHistory } from 'vue-router'
import { useLoginStore } from "@/stores/loginStore.js";


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'layout',
      redirect: "/",
      component: () => import('@/views/Layout.vue'),
      children: [{
        path: "/agent",
        name: "智能购物",
        component: () => import('@/views/agent/Agent.vue'),
        meta: {
          showFooter: false,
          checkLogin: true
        }
      }, {
        path: "/",
        name: "首页",
        component: () => import('@/views/Index.vue'),
      }, {
        path: "/product/:pCategoryId",
        name: "一级分类",
        component: () => import('@/views/CategoryProduct.vue'),
        meta: {
          showCategory: true
        }
      }, {
        path: "/product/:pCategoryId/:categoryId",
        name: "二级分类",
        component: () => import('@/views/CategoryProduct.vue'),
        meta: {
          showCategory: true
        }
      }, {
        path: "/detail/:productId",
        name: "详情",
        component: () => import('@/views/product/ProductDetail.vue'),
      }, {
        path: "/shopCart",
        name: "购物车",
        component: () => import('@/views/my/ShopCart.vue'),
        meta: {
          checkLogin: true
        }
      }, {
        path: "/my",
        name: "我的",
        redirect: "/my/shop",
        component: () => import('@/views/my/MyLayout.vue'),
        children: [{
          path: "/my/shop",
          name: "我的商城",
          component: () => import('@/views/my/Index.vue'),
          meta: {
            checkLogin: true
          }
        }, {
          path: "/my/orders",
          name: "我的订单",
          component: () => import('@/views/my/order/Orders.vue'),
          meta: {
            checkLogin: true
          }
        }, {
          path: "/my/comment",
          name: "我的评论",
          component: () => import('@/views/my/comment/MyComment.vue'),
          meta: {
            checkLogin: true
          }
        }, {
          path: "/my/address",
          name: "收货地址",
          component: () => import('@/views/my/Address.vue'),
          meta: {
            checkLogin: true
          }
        }, {
          path: "/my/userInfo",
          name: "个人信息",
          component: () => import('@/views/my/UserInfo.vue'),
          meta: {
            checkLogin: true
          }
        }]
      }, {
        path: "/search",
        name: "搜索",
        component: () => import('@/views/Search.vue'),
      }]
    },
  ],
})
/* router.beforeEach((to, from, next) => {
  const loginStore = useLoginStore();
  // 进行身份验证检查
  if (to.meta.login != null && to.meta.login && Object.keys(loginStore.userInfo).length == 0) {
    loginStore.showLogin = true;
  } else {
    next()
  }
}) */
export default router
