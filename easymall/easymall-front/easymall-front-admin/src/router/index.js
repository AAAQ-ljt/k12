import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/account/Account.vue'),
    },
    {
      path: '/',
      name: 'Layout',
      redirect: "/login",
      component: () => import('@/views/Layout.vue'),
      children: [
        {
          path: '/home',
          name: 'home',
          component: () => import('@/views/home/Home.vue'),
          meta: {
            itemList: ["首页"]
          }
        },
        {
          path: '/product/category',
          name: 'category',
          component: () => import('@/views/product/Category.vue'),
          meta: {
            itemList: ["商品", "分类管理"]
          }
        },
        {
          path: '/product/ProductProperty',
          name: 'productProperty',
          component: () => import('@/views/product/ProductProperty.vue'),
          meta: {
            itemList: ["商品", "商品属性"]
          }
        },
        {
          path: '/product/addProduct',
          name: 'addProduct',
          component: () => import('@/views/product/edit/ProductEdit.vue'),
          meta: {
            itemList: ["商品", "商品管理"]
          }
        },
        {
          path: '/product/updateProduct/:productId',
          name: 'updateProduct',
          component: () => import('@/views/product/edit/ProductEdit.vue'),
          meta: {
            itemList: ["商品", "商品管理"]
          }
        },
        {
          path: '/product',
          name: 'product',
          component: () => import('@/views/product/ProductList.vue'),
          meta: {
            itemList: ["商品", "商品管理"]
          }
        },
        {
          path: '/order/orderList',
          name: '订单管理',
          component: () => import('@/views/order/OrderList.vue'),
          meta: {
            itemList: ["订单", "订单管理"]
          }
        },
        {
          path: '/order/comment',
          name: '订单评论',
          component: () => import('@/views/order/OrderCommentList.vue'),
          meta: {
            itemList: ["订单", "订单评论"]
          }
        },
        {
          path: '/user/userList',
          name: '用户管理',
          component: () => import('@/views/user/UserList.vue'),
          meta: {
            itemList: ["用户管理", "用户列表"]
          }
        },
        {
          path: '/setting/logistics',
          name: '发货地址',
          component: () => import('@/views/setting/Logistics.vue'),
          meta: {
            itemList: ["设置", "发货地址"]
          }
        },
        {
          path: '/setting/prompt',
          name: '提示词',
          component: () => import('@/views/setting/Prompt.vue'),
          meta: {
            itemList: ["设置", "提示词管理"]
          }
        },
        {
          path: '/setting/rag',
          name: 'RAG知识库',
          component: () => import('@/views/setting/Rag.vue'),
          meta: {
            itemList: ["设置", "RAG知识库"]
          }
        },
      ],
    },
  ],
})

export default router
