<template>
  <div class="payment-channel">
    <n-card>
      <template #header>
        <span>支付渠道管理</span>
      </template>
      
      <n-alert type="info" style="margin-bottom: 16px;">
        <template #header>配置说明</template>
        点击"配置"按钮可编辑支付渠道参数，点击状态开关可启用/禁用渠道
      </n-alert>
      
      <n-data-table :columns="columns" :data="channels" :loading="loading" />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" title="支付渠道配置" style="width: 600px; max-width: 95vw;">
      <n-form ref="formRef" :model="form" label-placement="left" label-width="120px">
        <n-form-item label="渠道名称">
          <n-input v-model:value="form.channelName" disabled />
        </n-form-item>
        <n-form-item label="API地址">
          <n-input v-model:value="form.apiUrl" placeholder="请输入API地址" />
        </n-form-item>
        <n-form-item label="AppId">
          <n-input v-model:value="form.appId" placeholder="请输入AppId" />
        </n-form-item>
        <n-form-item label="AppSecret">
          <n-input v-model:value="form.appSecret" type="password" placeholder="请输入AppSecret" show-password-on="click" />
        </n-form-item>
        <n-form-item label="商户ID">
          <n-input v-model:value="form.merchantId" placeholder="请输入商户ID" />
        </n-form-item>
        <n-form-item label="商户私钥">
          <n-input v-model:value="form.merchantPrivateKey" type="textarea" :rows="3" placeholder="请输入商户私钥" />
        </n-form-item>
        <n-form-item label="平台公钥">
          <n-input v-model:value="form.platformPublicKey" type="textarea" :rows="3" placeholder="请输入平台公钥" />
        </n-form-item>
        <n-form-item label="回调地址">
          <n-input v-model:value="form.notifyUrl" placeholder="请输入回调地址" />
        </n-form-item>
        <n-form-item label="返回地址">
          <n-input v-model:value="form.returnUrl" placeholder="请输入返回地址" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sort" :min="1" :max="100" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NTag, NButton, NSwitch, NSpace, useMessage } from 'naive-ui'
import { paymentApi } from '@/api/payment'

const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const channels = ref([])
const formRef = ref(null)
const message = useMessage()

const form = ref({})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '渠道名称', key: 'channelName', width: 120 },
  { title: '渠道编码', key: 'channelCode', width: 100 },
  { 
    title: '渠道类型', 
    key: 'channelType',
    width: 100,
    render: (row) => {
      const types = { 1: { type: 'info', text: '支付宝' }, 2: { type: 'success', text: '微信' }, 3: { type: 'warning', text: '易支付' } }
      const item = types[row.channelType] || { type: 'default', text: '未知' }
      return h(NTag, { type: item.type, size: 'small' }, { default: () => item.text })
    }
  },
  { title: 'API地址', key: 'apiUrl', ellipsis: { tooltip: true } },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render: (row) => h(NSwitch, {
      value: row.status === 1,
      loading: row._loading,
      onUpdateValue: (val) => handleStatusChange(row, val)
    })
  },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render: (row) => h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '配置' })
  }
]

const loadChannels = async () => {
  loading.value = true
  try {
    const res = await paymentApi.getChannelList()
    if (res.code === 200) {
      channels.value = res.data.map(c => ({ ...c, _loading: false }))
    }
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  form.value = { ...row }
  showModal.value = true
}

const handleStatusChange = async (row, enabled) => {
  row._loading = true
  try {
    const res = enabled 
      ? await paymentApi.enableChannel(row.id)
      : await paymentApi.disableChannel(row.id)
    if (res.code === 200) {
      row.status = enabled ? 1 : 0
      message.success(enabled ? '已启用' : '已禁用')
    } else {
      message.error(res.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  } finally {
    row._loading = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await paymentApi.updateChannel(form.value)
    if (res.code === 200) {
      message.success('保存成功')
      showModal.value = false
      loadChannels()
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (e) {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadChannels()
})
</script>

<style scoped>
.payment-channel {
  padding: 0;
}
</style>
