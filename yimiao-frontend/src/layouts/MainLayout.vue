<template>
  <n-layout has-sider>
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <span v-if="!collapsed">疫苗预约系统</span>
        <span v-else>YM</span>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-left">
          <n-button 
            quaternary 
            circle 
            class="mobile-menu-btn"
            @click="collapsed = !collapsed"
          >
            <template #icon>
              <n-icon><MenuOutline /></n-icon>
            </template>
          </n-button>
        </div>
        <div class="header-right">
          <n-dropdown :options="userOptions" @select="handleUserSelect">
            <n-button text>
              <template #icon>
                <n-icon><PersonOutline /></n-icon>
              </template>
              <span class="username">{{ userStore.userInfo?.username || '用户' }}</span>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>
      <n-layout-content class="content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { NIcon, useDialog, useMessage } from 'naive-ui'
import {
  PersonOutline,
  HomeOutline,
  MedicalOutline,
  CalendarOutline,
  PeopleOutline,
  OpenOutline,
  LogOutOutline,
  MenuOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const dialog = useDialog()
const message = useMessage()
const collapsed = ref(false)

const activeKey = computed(() => route.name)

const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  {
    label: '首页',
    key: 'Home',
    icon: renderIcon(HomeOutline)
  },
  {
    label: '疫苗列表',
    key: 'VaccineList',
    icon: renderIcon(MedicalOutline)
  },
  {
    label: '我的预约',
    key: 'MyAppointments',
    icon: renderIcon(CalendarOutline)
  },
  {
    label: '接种人管理',
    key: 'VaccineeList',
    icon: renderIcon(PeopleOutline)
  }
]

const userOptions = computed(() => {
  const opts = [
    { label: '个人中心', key: 'profile', icon: renderIcon(PersonOutline) }
  ]
  
  if (userStore.userType === 1) {
    opts.push({ label: '管理后台', key: 'admin', icon: renderIcon(OpenOutline) })
  }
  
  opts.push({ label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) })
  
  return opts
})

const handleMenuSelect = (key) => {
  router.push({ name: key })
}

const handleUserSelect = (key) => {
  if (key === 'logout') {
    dialog.warning({
      title: '提示',
      content: '确定要退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: () => {
        userStore.logout()
        router.push('/login')
        message.success('已退出登录')
      }
    })
  } else if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'admin') {
    window.open('/admin/dashboard', '_blank')
  }
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #18a058;
  border-bottom: 1px solid #e8e8e8;
}

.header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mobile-menu-btn {
  display: none;
}

.content {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .username {
    display: none;
  }
  
  .content {
    padding: 0;
  }
}

@media (max-width: 480px) {
  .logo {
    font-size: 14px;
  }
}
</style>
