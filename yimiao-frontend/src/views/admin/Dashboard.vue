<template>
  <div class="dashboard">
    <n-grid :cols="24" :x-gap="16" :y-gap="16" responsive="screen">
      <n-gi :span="24" :sm="12" :md="6">
        <n-card>
          <n-statistic label="总用户数" :value="overview.totalUsers || 0">
            <template #prefix>
              <n-icon color="#18a058"><PeopleOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi :span="24" :sm="12" :md="6">
        <n-card>
          <n-statistic label="疫苗种类" :value="overview.totalVaccines || 0">
            <template #prefix>
              <n-icon color="#2080f0"><MedicalOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi :span="24" :sm="12" :md="6">
        <n-card>
          <n-statistic label="预约总数" :value="overview.totalAppointments || 0">
            <template #prefix>
              <n-icon color="#f0a020"><CalendarOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi :span="24" :sm="12" :md="6">
        <n-card>
          <n-statistic label="已完成接种" :value="overview.completedAppointments || 0">
            <template #prefix>
              <n-icon color="#18a058"><CheckmarkCircleOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>

    <n-grid :cols="24" :x-gap="16" :y-gap="16" responsive="screen" style="margin-top: 16px;">
      <n-gi :span="24" :md="12">
        <n-card title="快捷操作">
          <n-space wrap>
            <n-button type="primary" @click="$router.push('/admin/vaccines')">
              <template #icon><n-icon><MedicalOutline /></n-icon></template>
              管理疫苗
            </n-button>
            <n-button type="info" @click="$router.push('/admin/slots')">
              <template #icon><n-icon><TimeOutline /></n-icon></template>
              号源管理
            </n-button>
            <n-button type="warning" @click="$router.push('/admin/sites')">
              <template #icon><n-icon><LocationOutline /></n-icon></template>
              接种点管理
            </n-button>
            <n-button @click="$router.push('/admin/orders')">
              <template #icon><n-icon><DocumentTextOutline /></n-icon></template>
              查看订单
            </n-button>
          </n-space>
        </n-card>
      </n-gi>
      <n-gi :span="24" :md="12">
        <n-card title="系统状态">
          <n-descriptions label-placement="left" :column="2" bordered size="small">
            <n-descriptions-item label="网关服务">
              <n-tag :type="services.gateway ? 'success' : 'error'" size="small">
                {{ services.gateway ? '正常' : '异常' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="用户服务">
              <n-tag :type="services.user ? 'success' : 'error'" size="small">
                {{ services.user ? '正常' : '异常' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="疫苗服务">
              <n-tag :type="services.vaccine ? 'success' : 'error'" size="small">
                {{ services.vaccine ? '正常' : '异常' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="预约服务">
              <n-tag :type="services.appointment ? 'success' : 'error'" size="small">
                {{ services.appointment ? '正常' : '异常' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="支付服务">
              <n-tag :type="services.payment ? 'success' : 'error'" size="small">
                {{ services.payment ? '正常' : '异常' }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="数据库">
              <n-tag type="success" size="small">正常</n-tag>
            </n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="最近预约" style="margin-top: 16px;">
      <template #header-extra>
        <n-button text type="primary" @click="$router.push('/admin/orders')">
          查看全部
        </n-button>
      </template>
      <n-data-table :columns="columns" :data="recentAppointments" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NTag } from 'naive-ui'
import { statisticsApi } from '@/api/appointment'
import {
  PeopleOutline,
  MedicalOutline,
  CalendarOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  LocationOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'

const loading = ref(false)
const overview = ref({})
const recentAppointments = ref([])
const services = reactive({
  gateway: true,
  user: true,
  vaccine: true,
  appointment: true,
  payment: true
})

const columns = [
  { title: '预约ID', key: 'id', width: 80 },
  { title: '接种人', key: 'vaccineeName', width: 100 },
  { title: '疫苗', key: 'vaccineName', width: 150 },
  { title: '接种点', key: 'siteName', width: 180 },
  { title: '预约时间', key: 'appointmentTime', width: 160 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const types = { 0: 'warning', 1: 'success', 2: 'default', 3: 'error' }
      const texts = { 0: '待接种', 1: '已完成', 2: '已取消', 3: '已过期' }
      return h(NTag, { type: types[row.status] || 'default', size: 'small' }, { default: () => texts[row.status] || '未知' })
    }
  }
]

const loadOverview = async () => {
  loading.value = true
  try {
    const res = await statisticsApi.getOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('加载概览数据失败', e)
  } finally {
    loading.value = false
  }
}

const checkServices = async () => {
  const ports = {
    gateway: 9000,
    user: 8083,
    vaccine: 8085,
    appointment: 8087,
    payment: 8089
  }
  
  for (const [name, port] of Object.entries(ports)) {
    try {
      const res = await fetch(`http://localhost:${port}/actuator/health`)
      const data = await res.json()
      services[name] = data.status === 'UP'
    } catch (e) {
      services[name] = false
    }
  }
}

onMounted(() => {
  loadOverview()
  checkServices()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>
