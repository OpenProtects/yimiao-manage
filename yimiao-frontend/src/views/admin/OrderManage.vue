<template>
  <div class="order-manage">
    <n-card>
      <template #header>
        <span>预约订单管理</span>
      </template>
      
      <n-space vertical size="large">
        <n-space wrap>
          <n-select
            v-model:value="searchForm.status"
            placeholder="订单状态"
            clearable
            style="width: 120px"
            :options="statusOptions"
          />
          <n-input
            v-model:value="searchForm.vaccineeName"
            placeholder="接种人姓名"
            clearable
            style="width: 150px"
          />
          <n-button type="primary" @click="loadOrders">搜索</n-button>
          <n-button @click="resetSearch">重置</n-button>
        </n-space>
        
        <n-data-table :columns="columns" :data="orders" :loading="loading" :pagination="pagination" />
      </n-space>
    </n-card>
    
    <n-modal v-model:show="showDetail" preset="card" title="订单详情" style="width: 600px; max-width: 95vw;">
      <n-descriptions label-placement="left" :column="2" bordered v-if="currentOrder">
        <n-descriptions-item label="订单号">{{ currentOrder.orderNo }}</n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="getStatusType(currentOrder.status)" size="small">
            {{ getStatusText(currentOrder.status) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="接种人">{{ currentOrder.vaccineeName }}</n-descriptions-item>
        <n-descriptions-item label="身份证号">{{ currentOrder.vaccineeIdCard }}</n-descriptions-item>
        <n-descriptions-item label="疫苗">{{ currentOrder.vaccineName }}</n-descriptions-item>
        <n-descriptions-item label="接种点">{{ currentOrder.siteName }}</n-descriptions-item>
        <n-descriptions-item label="预约时间">{{ currentOrder.appointmentTime }}</n-descriptions-item>
        <n-descriptions-item label="金额">{{ currentOrder.amount ? `¥${currentOrder.amount}` : '免费' }}</n-descriptions-item>
        <n-descriptions-item label="创建时间">{{ currentOrder.createTime }}</n-descriptions-item>
        <n-descriptions-item label="备注">{{ currentOrder.remark || '-' }}</n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { NTag, NButton, NSpace, useDialog, useMessage } from 'naive-ui'

const loading = ref(false)
const showDetail = ref(false)
const orders = ref([])
const currentOrder = ref(null)
const dialog = useDialog()
const message = useMessage()

const searchForm = reactive({
  status: null,
  vaccineeName: ''
})

const statusOptions = [
  { label: '待接种', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已过期', value: 3 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    loadOrders()
  }
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'default', 3: 'error' }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = { 0: '待接种', 1: '已完成', 2: '已取消', 3: '已过期' }
  return texts[status] || '未知'
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '订单号', key: 'orderNo', width: 160 },
  { title: '接种人', key: 'vaccineeName', width: 80 },
  { title: '疫苗', key: 'vaccineName', width: 120 },
  { title: '接种点', key: 'siteName', width: 120 },
  { title: '预约时间', key: 'appointmentTime', width: 140 },
  { 
    title: '状态', 
    key: 'status',
    width: 80,
    render: (row) => h(NTag, { type: getStatusType(row.status), size: 'small' }, { default: () => getStatusText(row.status) })
  },
  { 
    title: '金额', 
    key: 'amount',
    width: 80,
    render: (row) => row.amount ? `¥${row.amount}` : '免费'
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => viewDetail(row) }, { default: () => '详情' }),
        row.status === 0 ? h(NButton, { size: 'small', type: 'success', onClick: () => completeOrder(row) }, { default: () => '完成' }) : null,
        row.status === 0 ? h(NButton, { size: 'small', type: 'warning', onClick: () => cancelOrder(row) }, { default: () => '取消' }) : null
      ].filter(Boolean)
    })
  }
]

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.status = null
  searchForm.vaccineeName = ''
  loadOrders()
}

const viewDetail = (row) => {
  currentOrder.value = row
  showDetail.value = true
}

const completeOrder = (row) => {
  dialog.warning({
    title: '提示',
    content: '确定要将此订单标记为已完成吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      message.success('操作成功')
      loadOrders()
    }
  })
}

const cancelOrder = (row) => {
  dialog.warning({
    title: '提示',
    content: '确定要取消此预约吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      message.success('取消成功')
      loadOrders()
    }
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-manage {
  padding: 0;
}
</style>
