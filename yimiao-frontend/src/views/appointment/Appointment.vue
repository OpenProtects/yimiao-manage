<template>
  <div class="appointment-page">
    <n-card title="预约接种" :bordered="false">
      <n-steps :current="currentStep" :status="currentStatus">
        <n-step title="选择接种人" />
        <n-step title="选择疫苗" />
        <n-step title="选择时间" />
        <n-step title="确认预约" />
      </n-steps>

      <div class="step-content">
        <div v-if="currentStep === 1">
          <n-space vertical>
            <n-radio-group v-model:value="selectedVaccinee">
              <n-space>
                <n-radio-button v-for="v in vaccineeList" :key="v.id" :value="v.id">
                  {{ v.realName }} ({{ v.idCard | maskIdCard }})
                </n-radio-button>
              </n-space>
            </n-radio-group>
            <n-empty v-if="vaccineeList.length === 0" description="暂无接种人，请先添加">
              <template #extra>
                <n-button @click="$router.push('/vaccinee')">添加接种人</n-button>
              </template>
            </n-empty>
          </n-space>
        </div>

        <div v-if="currentStep === 2">
          <n-grid :cols="3" :x-gap="16" :y-gap="16">
            <n-grid-item v-for="vaccine in vaccineList" :key="vaccine.id">
              <n-card :title="vaccine.name" hoverable @click="selectVaccine(vaccine)">
                <p>类型: {{ vaccine.type }}</p>
                <p>厂家: {{ vaccine.manufacturer }}</p>
                <p>价格: {{ vaccine.isFree ? '免费' : '¥' + vaccine.price }}</p>
              </n-card>
            </n-grid-item>
          </n-grid>
        </div>

        <div v-if="currentStep === 3">
          <n-calendar v-model:value="selectedDate" #="{ year, month, date }">
            {{ date }}
          </n-calendar>
          <n-space class="time-slots" v-if="slotList.length > 0">
            <n-button v-for="slot in slotList" :key="slot.id" 
                      :type="selectedSlot === slot.id ? 'primary' : 'default'"
                      @click="selectedSlot = slot.id">
              {{ slot.startTime }} - {{ slot.endTime }}
              <n-badge :value="slot.remainCount" :max="99" />
            </n-button>
          </n-space>
        </div>

        <div v-if="currentStep === 4">
          <n-descriptions label-placement="left" :column="1">
            <n-descriptions-item label="接种人">{{ selectedVaccineeName }}</n-descriptions-item>
            <n-descriptions-item label="疫苗">{{ selectedVaccineName }}</n-descriptions-item>
            <n-descriptions-item label="接种点">{{ selectedSiteName }}</n-descriptions-item>
            <n-descriptions-item label="预约时间">{{ selectedDateTime }}</n-descriptions-item>
            <n-descriptions-item label="费用">{{ selectedVaccinePrice }}</n-descriptions-item>
          </n-descriptions>
        </div>
      </div>

      <div class="step-actions">
        <n-button v-if="currentStep > 1" @click="prevStep">上一步</n-button>
        <n-button v-if="currentStep < 4" type="primary" @click="nextStep" :disabled="!canNext">
          下一步
        </n-button>
        <n-button v-if="currentStep === 4" type="primary" @click="submitAppointment" :loading="submitting">
          确认预约
        </n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import request from '@/api/request'

const router = useRouter()
const message = useMessage()

const currentStep = ref(1)
const currentStatus = ref('process')
const submitting = ref(false)

const vaccineeList = ref([])
const vaccineList = ref([])
const slotList = ref([])

const selectedVaccinee = ref(null)
const selectedVaccine = ref(null)
const selectedDate = ref(Date.now())
const selectedSlot = ref(null)

const canNext = computed(() => {
  if (currentStep.value === 1) return !!selectedVaccinee.value
  if (currentStep.value === 2) return !!selectedVaccine.value
  if (currentStep.value === 3) return !!selectedSlot.value
  return true
})

const selectedVaccineeName = computed(() => {
  const v = vaccineeList.value.find(item => item.id === selectedVaccinee.value)
  return v?.realName || ''
})

const selectedVaccineName = computed(() => selectedVaccine.value?.name || '')
const selectedSiteName = computed(() => '待选择')
const selectedVaccinePrice = computed(() => {
  if (!selectedVaccine.value) return ''
  return selectedVaccine.value.isFree ? '免费' : `¥${selectedVaccine.value.price}`
})

const selectedDateTime = computed(() => {
  if (!selectedDate.value || !selectedSlot.value) return ''
  const d = new Date(selectedDate.value)
  const slot = slotList.value.find(s => s.id === selectedSlot.value)
  return `${d.toLocaleDateString()} ${slot?.startTime || ''}`
})

const fetchVaccineeList = async () => {
  try {
    const res = await request.get('/user/vaccinee/list')
    vaccineeList.value = res.data || []
  } catch (e) {
    message.error('获取接种人列表失败')
  }
}

const fetchVaccineList = async () => {
  try {
    const res = await request.get('/vaccine/list')
    vaccineList.value = res.data || []
  } catch (e) {
    message.error('获取疫苗列表失败')
  }
}

const selectVaccine = (vaccine) => {
  selectedVaccine.value = vaccine
}

const nextStep = () => {
  if (currentStep.value < 4) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const submitAppointment = async () => {
  submitting.value = true
  try {
    await request.post('/appointment/create', {
      vaccineeId: selectedVaccinee.value,
      vaccineId: selectedVaccine.value.id,
      slotId: selectedSlot.value
    })
    message.success('预约成功')
    router.push('/my-appointments')
  } catch (e) {
    message.error('预约失败: ' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchVaccineeList()
  fetchVaccineList()
})
</script>

<style scoped>
.appointment-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.step-content {
  margin: 24px 0;
  min-height: 300px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.time-slots {
  margin-top: 16px;
}
</style>
