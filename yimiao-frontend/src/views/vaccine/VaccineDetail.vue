<template>
  <div class="page-container">
    <n-card v-if="vaccine" :title="vaccine.name">
      <n-descriptions :column="2" bordered>
        <n-descriptions-item label="疫苗编码">{{ vaccine.code }}</n-descriptions-item>
        <n-descriptions-item label="疫苗类型">{{ vaccine.type }}</n-descriptions-item>
        <n-descriptions-item label="生产厂家">{{ vaccine.manufacturer }}</n-descriptions-item>
        <n-descriptions-item label="规格">{{ vaccine.specification }}</n-descriptions-item>
        <n-descriptions-item label="适用年龄">{{ vaccine.minAge }}-{{ vaccine.maxAge }}岁</n-descriptions-item>
        <n-descriptions-item label="接种剂次">{{ vaccine.doseCount }}针</n-descriptions-item>
        <n-descriptions-item label="剂次间隔">{{ vaccine.doseInterval }}天</n-descriptions-item>
        <n-descriptions-item label="价格">
          <n-tag :type="vaccine.isFree ? 'success' : 'warning'">
            {{ vaccine.isFree ? '免费' : `¥${vaccine.price}` }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="描述" :span="2">
          {{ vaccine.description || '暂无描述' }}
        </n-descriptions-item>
      </n-descriptions>
      
      <n-divider />
      
      <n-h3>选择接种点</n-h3>
      <n-select
        v-model:value="selectedSite"
        :options="siteOptions"
        placeholder="请选择接种点"
        style="margin-bottom: 20px"
      />
      
      <n-h3>选择日期</n-h3>
      <n-date-picker
        v-model:value="selectedDate"
        type="date"
        :is-date-disabled="isDateDisabled"
        style="margin-bottom: 20px"
      />
      
      <n-h3>选择时间段</n-h3>
      <n-grid v-if="slots.length > 0" :cols="4" :x-gap="10" :y-gap="10">
        <n-gi v-for="slot in slots" :key="slot.id">
          <n-card
            size="small"
            hoverable
            :class="{ 'slot-selected': selectedSlot === slot.id }"
            @click="selectSlot(slot)"
          >
            <p>{{ slot.startTime }} - {{ slot.endTime }}</p>
            <p class="slot-remain">剩余: {{ slot.remainCount }}</p>
          </n-card>
        </n-gi>
      </n-grid>
      <n-empty v-else description="暂无可预约时段" />
      
      <n-divider />
      
      <n-button
        type="primary"
        size="large"
        :disabled="!selectedSlot"
        :loading="loading"
        @click="handleAppointment"
      >
        立即预约
      </n-button>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { vaccineApi, siteApi, slotApi } from '@/api/vaccine'
import { appointmentApi } from '@/api/appointment'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const vaccine = ref(null)
const sites = ref([])
const slots = ref([])
const selectedSite = ref(null)
const selectedDate = ref(null)
const selectedSlot = ref(null)
const loading = ref(false)

const siteOptions = computed(() => 
  sites.value.map(s => ({ label: s.name, value: s.id }))
)

const loadVaccine = async () => {
  const res = await vaccineApi.getDetail(route.params.id)
  if (res.code === 200) {
    vaccine.value = res.data
  }
}

const loadSites = async () => {
  const res = await siteApi.getAll()
  if (res.code === 200) {
    sites.value = res.data
  }
}

const loadSlots = async () => {
  if (!selectedSite.value || !selectedDate.value) {
    slots.value = []
    return
  }
  
  const res = await slotApi.getAvailable(
    selectedSite.value,
    vaccine.value.id,
    dayjs(selectedDate.value).format('YYYY-MM-DD')
  )
  
  if (res.code === 200) {
    slots.value = res.data
  }
}

watch([selectedSite, selectedDate], () => {
  selectedSlot.value = null
  if (vaccine.value) {
    loadSlots()
  }
})

const isDateDisabled = (ts) => {
  const date = dayjs(ts)
  return date.isBefore(dayjs(), 'day')
}

const selectSlot = (slot) => {
  if (slot.remainCount > 0) {
    selectedSlot.value = slot.id
  }
}

const handleAppointment = async () => {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const res = await appointmentApi.create({
      userId: userStore.userId,
      vaccineeId: null,
      vaccineId: vaccine.value.id,
      siteId: selectedSite.value,
      slotId: selectedSlot.value,
      doseNo: 1
    })
    
    if (res.code === 200) {
      message.success('预约成功')
      router.push('/my-appointments')
    } else {
      message.error(res.message || '预约失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadVaccine()
  await loadSites()
})
</script>

<style scoped>
.slot-selected {
  border: 2px solid #18a058;
}

.slot-remain {
  color: #999;
  font-size: 12px;
}
</style>
