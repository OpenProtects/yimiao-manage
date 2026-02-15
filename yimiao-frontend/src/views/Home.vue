<template>
  <div class="page-container">
    <n-grid :cols="4" :x-gap="20" :y-gap="20">
      <n-gi>
        <n-card>
          <n-statistic label="总用户数" :value="statistics.totalUsers || 0">
            <template #prefix>
              <n-icon color="#18a058"><PeopleOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <n-statistic label="疫苗种类" :value="statistics.totalVaccines || 0">
            <template #prefix>
              <n-icon color="#2080f0"><MedicalOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <n-statistic label="预约总数" :value="statistics.totalAppointments || 0">
            <template #prefix>
              <n-icon color="#f0a020"><CalendarOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card>
          <n-statistic label="已完成接种" :value="statistics.completedAppointments || 0">
            <template #prefix>
              <n-icon color="#18a058"><CheckmarkCircleOutline /></n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>

    <n-card title="热门疫苗" class="mt-20">
      <n-data-table :columns="vaccineColumns" :data="vaccineStats" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NTag } from 'naive-ui'
import {
  PeopleOutline,
  MedicalOutline,
  CalendarOutline,
  CheckmarkCircleOutline
} from '@vicons/ionicons5'
import { statisticsApi } from '@/api/appointment'

const loading = ref(false)
const statistics = ref({})
const vaccineStats = ref([])

const vaccineColumns = [
  { title: '疫苗名称', key: 'name' },
  { title: '疫苗编码', key: 'code' },
  { title: '预约次数', key: 'appointment_count' },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: 'success' }, { default: () => '正常' })
  }
]

const loadStatistics = async () => {
  loading.value = true
  try {
    const [overviewRes, vaccineRes] = await Promise.all([
      statisticsApi.getOverview(),
      statisticsApi.getVaccine()
    ])
    
    if (overviewRes.code === 200) {
      statistics.value = overviewRes.data
    }
    if (vaccineRes.code === 200) {
      vaccineStats.value = vaccineRes.data.vaccineStats || []
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}
</style>
