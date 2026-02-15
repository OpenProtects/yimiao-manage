import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'vaccine',
        name: 'VaccineList',
        component: () => import('@/views/vaccine/VaccineList.vue'),
        meta: { title: '疫苗列表' }
      },
      {
        path: 'vaccine/:id',
        name: 'VaccineDetail',
        component: () => import('@/views/vaccine/VaccineDetail.vue'),
        meta: { title: '疫苗详情' }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/appointment/Appointment.vue'),
        meta: { title: '预约接种', requiresAuth: true }
      },
      {
        path: 'my-appointments',
        name: 'MyAppointments',
        component: () => import('@/views/appointment/MyAppointments.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      {
        path: 'vaccinee',
        name: 'VaccineeList',
        component: () => import('@/views/vaccinee/VaccineeList.vue'),
        meta: { title: '接种人管理', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'sites',
        name: 'SiteManage',
        component: () => import('@/views/admin/SiteManage.vue'),
        meta: { title: '接种点管理' }
      },
      {
        path: 'vaccines',
        name: 'VaccineManage',
        component: () => import('@/views/admin/VaccineManage.vue'),
        meta: { title: '疫苗管理' }
      },
      {
        path: 'slots',
        name: 'SlotManage',
        component: () => import('@/views/admin/SlotManage.vue'),
        meta: { title: '号源管理' }
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'payment-channels',
        name: 'PaymentChannel',
        component: () => import('@/views/admin/PaymentChannel.vue'),
        meta: { title: '支付渠道管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  
  if (to.meta.requiresAdmin && userStore.userType !== 1) {
    next({ name: 'Home' })
    return
  }
  
  document.title = to.meta.title ? `${to.meta.title} - 疫苗预约系统` : '疫苗预约系统'
  next()
})

export default router
