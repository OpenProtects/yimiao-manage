<template>
  <div class="site-manage">
    <n-card>
      <template #header>
        <div class="card-header">
          <span>接种点管理</span>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            添加接种点
          </n-button>
        </div>
      </template>
      
      <n-data-table :columns="columns" :data="sites" :loading="loading" />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑接种点' : '添加接种点'" style="width: 600px; max-width: 95vw;">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100px">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入接种点名称" />
        </n-form-item>
        <n-form-item label="编码" path="code">
          <n-input v-model:value="form.code" placeholder="请输入接种点编码" />
        </n-form-item>
        <n-form-item label="区域" path="region">
          <n-input v-model:value="form.region" placeholder="请输入区域" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="form.address" placeholder="请输入详细地址" />
        </n-form-item>
        <n-form-item label="电话">
          <n-input v-model:value="form.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="营业时间">
          <n-input v-model:value="form.businessHours" placeholder="如: 08:00-17:00" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NTag, NButton, NSpace, useDialog, useMessage } from 'naive-ui'
import { siteApi } from '@/api/vaccine'
import { AddOutline } from '@vicons/ionicons5'

const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const sites = ref([])
const formRef = ref(null)
const dialog = useDialog()
const message = useMessage()

const form = ref({
  name: '',
  code: '',
  region: '',
  address: '',
  phone: '',
  businessHours: '08:00-17:00',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  region: [{ required: true, message: '请输入区域', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '名称', key: 'name', width: 180 },
  { title: '编码', key: 'code', width: 100 },
  { title: '区域', key: 'region', width: 100 },
  { title: '地址', key: 'address', ellipsis: { tooltip: true } },
  { title: '电话', key: 'phone', width: 120 },
  { title: '营业时间', key: 'businessHours', width: 120 },
  { 
    title: '状态', 
    key: 'status',
    width: 80,
    render: (row) => h(NTag, { type: row.status === 0 ? 'success' : 'error', size: 'small' }, { default: () => row.status === 0 ? '正常' : '禁用' })
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' })
      ]
    })
  }
]

const loadSites = async () => {
  loading.value = true
  try {
    const res = await siteApi.getAll()
    if (res.code === 200) {
      sites.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  showModal.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  showModal.value = true
}

const handleDelete = (row) => {
  dialog.warning({
    title: '提示',
    content: '确定要删除该接种点吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const res = await siteApi.delete(row.id)
      if (res.code === 200) {
        message.success('删除成功')
        loadSites()
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    const res = form.value.id
      ? await siteApi.update(form.value)
      : await siteApi.add(form.value)
    
    if (res.code === 200) {
      message.success('操作成功')
      showModal.value = false
      resetForm()
      loadSites()
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.value = {
    name: '', code: '', region: '', address: '',
    phone: '', businessHours: '08:00-17:00', description: ''
  }
}

onMounted(() => {
  loadSites()
})
</script>

<style scoped>
.site-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
