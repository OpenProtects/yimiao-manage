<template>
  <div class="vaccine-manage">
    <n-card>
      <template #header>
        <div class="card-header">
          <span>疫苗管理</span>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            添加疫苗
          </n-button>
        </div>
      </template>
      
      <n-data-table :columns="columns" :data="vaccines" :loading="loading" :pagination="pagination" />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '编辑疫苗' : '添加疫苗'" style="width: 700px; max-width: 95vw;">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100px">
        <n-grid :cols="24" :x-gap="16">
          <n-gi :span="12">
            <n-form-item label="名称" path="name">
              <n-input v-model:value="form.name" placeholder="请输入疫苗名称" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="编码" path="code">
              <n-input v-model:value="form.code" placeholder="请输入疫苗编码" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="类型" path="type">
              <n-input v-model:value="form.type" placeholder="请输入疫苗类型" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="厂家" path="manufacturer">
              <n-input v-model:value="form.manufacturer" placeholder="请输入生产厂家" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="最小年龄">
              <n-input-number v-model:value="form.minAge" :min="0" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="最大年龄">
              <n-input-number v-model:value="form.maxAge" :min="0" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="剂次">
              <n-input-number v-model:value="form.doseCount" :min="1" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="间隔天数">
              <n-input-number v-model:value="form.doseInterval" :min="0" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="价格">
              <n-input-number v-model:value="form.price" :min="0" :precision="2" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi :span="12">
            <n-form-item label="是否免费">
              <n-switch v-model:value="form.isFree" />
            </n-form-item>
          </n-gi>
        </n-grid>
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
import { ref, reactive, onMounted, h } from 'vue'
import { NTag, NButton, NSpace, useDialog, useMessage } from 'naive-ui'
import { vaccineApi } from '@/api/vaccine'
import { AddOutline } from '@vicons/ionicons5'

const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const vaccines = ref([])
const formRef = ref(null)
const dialog = useDialog()
const message = useMessage()

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    loadVaccines()
  }
})

const form = ref({
  name: '',
  code: '',
  type: '',
  manufacturer: '',
  minAge: 0,
  maxAge: 150,
  doseCount: 1,
  doseInterval: 28,
  price: 0,
  isFree: true,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '名称', key: 'name', width: 150 },
  { title: '编码', key: 'code', width: 120 },
  { title: '类型', key: 'type', width: 100 },
  { title: '厂家', key: 'manufacturer', width: 120 },
  { 
    title: '适用年龄', 
    key: 'age',
    width: 100,
    render: (row) => `${row.minAge}-${row.maxAge}岁`
  },
  { title: '剂次', key: 'doseCount', width: 60 },
  { 
    title: '价格', 
    key: 'price',
    width: 80,
    render: (row) => row.isFree ? h(NTag, { type: 'success', size: 'small' }, { default: () => '免费' }) : `¥${row.price}`
  },
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

const loadVaccines = async () => {
  loading.value = true
  try {
    const res = await vaccineApi.getPage({
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      vaccines.value = res.data.records
      pagination.itemCount = res.data.total
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
    content: '确定要删除该疫苗吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const res = await vaccineApi.delete(row.id)
      if (res.code === 200) {
        message.success('删除成功')
        loadVaccines()
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    const res = form.value.id
      ? await vaccineApi.update(form.value)
      : await vaccineApi.add(form.value)
    
    if (res.code === 200) {
      message.success('操作成功')
      showModal.value = false
      resetForm()
      loadVaccines()
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.value = {
    name: '', code: '', type: '', manufacturer: '',
    minAge: 0, maxAge: 150, doseCount: 1, doseInterval: 28,
    price: 0, isFree: true, description: ''
  }
}

onMounted(() => {
  loadVaccines()
})
</script>

<style scoped>
.vaccine-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
