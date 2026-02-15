<template>
  <div class="admin-layout">
    <n-layout has-sider>
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="0"
        :width="220"
        :collapsed="collapsed"
        show-trigger
        @collapse="collapsed = true"
        @expand="collapsed = false"
        :native-scrollbar="false"
        class="admin-sider"
        :mobile-style="{ maxWidth: '80vw' }"
      >
        <div class="logo">
          <span v-if="!collapsed">管理后台</span>
          <span v-else>管理</span>
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
        <n-layout-header bordered class="admin-header">
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
            <n-breadcrumb class="desktop-breadcrumb">
              <n-breadcrumb-item @click="$router.push('/admin/dashboard')">管理后台</n-breadcrumb-item>
              <n-breadcrumb-item>{{ currentPageTitle }}</n-breadcrumb-item>
            </n-breadcrumb>
          </div>
          <div class="header-right">
            <n-button quaternary @click="goToUserFront" class="back-btn">
              <template #icon>
                <n-icon><ExitOutline /></n-icon>
              </template>
              返回用户端
            </n-button>
            <n-dropdown :options="userOptions" @select="handleUserSelect">
              <n-button text>
                <template #icon>
                  <n-icon><PersonOutline /></n-icon>
                </template>
                <span class="username">{{ username }}</span>
              </n-button>
            </n-dropdown>
          </div>
        </n-layout-header>
        
        <n-layout-content class="admin-content">
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup>
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { NIcon, useDialog, useMessage } from 'naive-ui'
import {
  PersonOutline,
  GridOutline,
  MedicalOutline,
  LocationOutline,
  DocumentTextOutline,
  CardOutline,
  TimeOutline,
  LogOutOutline,
  ExitOutline,
  MenuOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const dialog = useDialog()
const message = useMessage()

const collapsed = ref(false)

const username = computed(() => userStore.userInfo?.username || '管理员')

const activeKey = computed(() => route.name)

const currentPageTitle = computed(() => {
  const titles = {
    'Dashboard': '数据概览',
    'VaccineManage': '疫苗管理',
    'SlotManage': '号源管理',
    'SiteManage': '接种点管理',
    'OrderManage': '预约订单',
    'PaymentChannel': '支付渠道'
  }
  return titles[route.name] || '管理后台'
})

const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  {
    label: '数据概览',
    key: 'Dashboard',
    icon: renderIcon(GridOutline)
  },
  {
    label: '疫苗管理',
    key: 'VaccineManage',
    icon: renderIcon(MedicalOutline)
  },
  {
    label: '号源管理',
    key: 'SlotManage',
    icon: renderIcon(TimeOutline)
  },
  {
    label: '接种点管理',
    key: 'SiteManage',
    icon: renderIcon(LocationOutline)
  },
  {
    label: '预约订单',
    key: 'OrderManage',
    icon: renderIcon(DocumentTextOutline)
  },
  {
    label: '支付渠道',
    key: 'PaymentChannel',
    icon: renderIcon(CardOutline)
  }
]

const userOptions = [
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) }
]

const handleMenuSelect = (key) => {
  router.push({ name: key })
  if (window.innerWidth < 768) {
    collapsed.value = true
  }
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
  }
}

const goToUserFront = () => {
  router.push('/home')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100%;
}

.admin-sider {
  background: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  color: #18a058;
  border-bottom: 1px solid #e8e8e8;
}

.admin-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #18a058;
}

.mobile-menu-btn {
  display: none;
}

.admin-content {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  padding: 20px;
}

@media (max-width: 768px) {
  .admin-header {
    padding: 0 12px;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .desktop-breadcrumb {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .back-btn {
    display: none;
  }
  
  .admin-content {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .admin-content {
    padding: 8px;
  }
  
  .logo {
    font-size: 14px;
  }
}
</style>
