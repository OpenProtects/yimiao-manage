<template>
  <div class="page-container">
    <n-card title="号源管理">
      <template #header-extra>
        <n-button type="primary" @click="showGenerateModal = true">批量生成号源</n-button>
      </template>
      
      <n-space class="mb-20">
        <n-select v-model:value="filters.siteId" :options="siteOptions" placeholder="选择接种点" style="width: 200px" />
        <n-select v-model:value="filters.vaccineId" :options="vaccineOptions" placeholder="选择疫苗" style="width: 200px" />
        <n-button @click="loadSlots">查询</n-button>
      </n-space>
      
      <n-data-table :columns="columns" :data="slots" :loading="loading" />
    </n-card>
    
    <n-modal v-model:show="showGenerateModal" preset="card" title="批量生成号源" style="width: 500px">
      <n-form ref="genFormRef" :model="genForm" :rules="genRules" label-placement="left">
        <n-form-item path="siteId" label="接种点">
          <n-select v-model:value="genForm.siteId" :options="siteOptions" />
        </n-form-item>
        <n-form-item path="vaccineId" label="疫苗">
          <n-select v-model:value="genForm.vaccineId" :options="vaccineOptions" />
        </n-form-item>
        <n-form-item path="startDate" label="开始日期">
          <n-date-picker v-model:value="genForm.startDateTs" type="date" />
        </n-form-item>
        <n-form-item path="endDate" label="结束日期">
          <n-date-picker v-model:value="genForm.endDateTs" type="date" />
        </n-form-item>
        <n-form-item path="dailyCount" label="每日数量">
          <n-input-number v-model:value="genForm.dailyCount" :min="1" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGenerateModal = false">取消</n-button>
          <n-button type="primary" :loading="generating" @click="handleGenerate">生成</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NTag } from 'naive-ui'
import { siteApi, vaccineApi, slotApi } from '@/api/vaccine'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'

const message = useMessage()
const loading = ref(false)
const generating = ref(false)
const showGenerateModal = ref(false)
const slots = ref([])
const sites = ref([])
const vaccines = ref([])

const filters = ref({ siteId: null, vaccineId: null })
const genForm = ref({
  siteId: null,
  vaccineId: null,
  startDateTs: null,
  endDateTs: null,
  dailyCount: 100
})

const genRules = {
  siteId: { required: true },
  vaccineId: { required: true },
  startDateTs: { required: true },
  endDateTs: { required: true },
  dailyCount: { required: true }
}

const siteOptions = computed(() => sites.value.map(s => ({ label: s.name, value: s.id })))
const vaccineOptions = computed(() => vaccines.value.map(v => ({ label: v.name, value: v.id })))

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '日期', key: 'slotDate' },
  { title: '时间段', key: 'time', render: (row) => `${row.startTime} - ${row.endTime}` },
  { title: '总数', key: 'totalCount' },
  { title: '已预约', key: 'bookedCount' },
  { title: '剩余', key: 'remainCount' },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const types = { 0: 'success', 1: 'warning', 2: 'default' }
      const texts = { 0: '可预约', 1: '已满', 2: '已过期' }
      return h(NTag, { type: types[row.status] }, { default: () => texts[row.status] })
    }
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NButton, { size: 'small', type: 'error', onClick: () => deleteSlot(row) }, 
      { default: () => '删除' })
  }
]

const loadSites = async () => {
  const res = await siteApi.getAll()
  if (res.code === 200) sites.value = res.data
}

const loadVaccines = async () => {
  const res = await vaccineApi.getAvailable()
  if (res.code === 200) vaccines.value = res.data
}

const loadSlots = async () => {
  loading.value = true
  try {
    const res = await slotApi.getPage({ pageNum: 1, pageSize: 100, ...filters.value })
    if (res.code === 200) slots.value = res.data.records
  } finally {
    loading.value = false
  }
}

const deleteSlot = async (row) => {
  const res = await slotApi.delete(row.id)
  if (res.code === 200) {
    message.success('删除成功')
    loadSlots()
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    const res = await slotApi.generate(
      genForm.value.siteId,
      genForm.value.vaccineId,
      dayjs(genForm.value.startDateTs).format('YYYY-MM-DD'),
      dayjs(genForm.value.endDateTs).format('YYYY-MM-DD'),
      genForm.value.dailyCount
    )
    if (res.code === 200) {
      message.success('生成成功')
      showGenerateModal.value = false
      loadSlots()
    }
  } finally {
    generating.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadSites(), loadVaccines()])
  loadSlots()
})
</script>

<style scoped>
.mb-20 {
  margin-bottom: 20px;
}
</style>
