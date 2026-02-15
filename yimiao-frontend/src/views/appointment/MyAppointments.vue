<template>
  <div class="page-container">
    <n-card title="我的预约">
      <n-list>
        <n-list-item v-for="item in appointments" :key="item.id">
          <n-thing :title="item.orderNo">
            <template #header-extra>
              <n-tag :type="getStatusType(item.status)">
                {{ getStatusText(item.status) }}
              </n-tag>
            </template>
            <n-descriptions :column="2">
              <n-descriptions-item label="疫苗">-</n-descriptions-item>
              <n-descriptions-item label="接种点">-</n-descriptions-item>
              <n-descriptions-item label="预约日期">-</n-descriptions-item>
              <n-descriptions-item label="剂次">第{{ item.doseNo }}针</n-descriptions-item>
            </n-descriptions>
            <template #footer>
              <n-button
                v-if="item.status === 0 || item.status === 1"
                type="error"
                size="small"
                @click="handleCancel(item)"
              >
                取消预约
              </n-button>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>
      
      <n-empty v-if="appointments.length === 0" description="暂无预约记录" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { appointmentApi } from '@/api/appointment'

const message = useMessage()
const dialog = useDialog()
const appointments = ref([])

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'default',
    3: 'success',
    4: 'error',
    5: 'info'
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已完成',
    4: '已过期',
    5: '已退款'
  }
  return texts[status] || '未知'
}

const loadAppointments = async () => {
  const res = await appointmentApi.getMyAppointments()
  if (res.code === 200) {
    appointments.value = res.data
  }
}

const handleCancel = (item) => {
  dialog.warning({
    title: '确认取消',
    content: '确定要取消此预约吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const res = await appointmentApi.cancel(item.id, '用户主动取消')
      if (res.code === 200) {
        message.success('取消成功')
        loadAppointments()
      } else {
        message.error(res.message || '取消失败')
      }
    }
  })
}

onMounted(() => {
  loadAppointments()
})
</script>
