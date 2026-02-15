<template>
  <div class="page-container">
    <n-card title="接种人管理">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">
          添加接种人
        </n-button>
      </template>
      
      <n-list>
        <n-list-item v-for="item in vaccinees" :key="item.id">
          <n-thing :title="item.realName">
            <template #header-extra>
              <n-tag v-if="item.isDefault" type="success" size="small">默认</n-tag>
            </template>
            <n-descriptions :column="3">
              <n-descriptions-item label="身份证">{{ item.idCard }}</n-descriptions-item>
              <n-descriptions-item label="性别">{{ item.gender === 1 ? '男' : '女' }}</n-descriptions-item>
              <n-descriptions-item label="年龄">{{ item.age }}岁</n-descriptions-item>
            </n-descriptions>
            <template #footer>
              <n-space>
                <n-button size="small" @click="editItem(item)">编辑</n-button>
                <n-button
                  v-if="!item.isDefault"
                  size="small"
                  @click="setDefault(item.id)"
                >
                  设为默认
                </n-button>
                <n-button size="small" type="error" @click="deleteItem(item)">
                  删除
                </n-button>
              </n-space>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>
      
      <n-empty v-if="vaccinees.length === 0" description="暂无接种人" />
    </n-card>
    
    <n-modal v-model:show="showModal" preset="card" title="添加接种人" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left">
        <n-form-item path="realName" label="姓名">
          <n-input v-model:value="form.realName" placeholder="请输入真实姓名" />
        </n-form-item>
        <n-form-item path="idCard" label="身份证">
          <n-input v-model:value="form.idCard" placeholder="请输入身份证号" />
        </n-form-item>
        <n-form-item path="gender" label="性别">
          <n-radio-group v-model:value="form.gender">
            <n-radio :value="1">男</n-radio>
            <n-radio :value="2">女</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item path="birthDate" label="出生日期">
          <n-date-picker
            v-model:value="birthDateTs"
            type="date"
            @update:value="updateBirthDate"
          />
        </n-form-item>
        <n-form-item path="phone" label="联系电话">
          <n-input v-model:value="form.phone" placeholder="请输入联系电话" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleSubmit">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { vaccineeApi } from '@/api/user'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const vaccinees = ref([])
const showModal = ref(false)
const loading = ref(false)
const formRef = ref(null)
const birthDateTs = ref(null)

const form = ref({
  id: null,
  userId: null,
  realName: '',
  idCard: '',
  gender: 1,
  birthDate: null,
  phone: '',
  isDefault: false
})

const rules = {
  realName: { required: true, message: '请输入姓名', trigger: 'blur' },
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  gender: { required: true },
  birthDate: { required: true, message: '请选择出生日期' }
}

const updateBirthDate = (ts) => {
  form.value.birthDate = ts ? dayjs(ts).format('YYYY-MM-DD') : null
}

const loadVaccinees = async () => {
  const res = await vaccineeApi.list()
  if (res.code === 200) {
    vaccinees.value = res.data
  }
}

const editItem = (item) => {
  form.value = { ...item, userId: userStore.userId }
  birthDateTs.value = item.birthDate ? new Date(item.birthDate).getTime() : null
  showModal.value = true
}

const setDefault = async (id) => {
  const res = await vaccineeApi.setDefault(id)
  if (res.code === 200) {
    message.success('设置成功')
    loadVaccinees()
  }
}

const deleteItem = (item) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除接种人"${item.realName}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const res = await vaccineeApi.delete(item.id)
      if (res.code === 200) {
        message.success('删除成功')
        loadVaccinees()
      }
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    form.value.userId = userStore.userId
    const res = form.value.id
      ? await vaccineeApi.update(form.value)
      : await vaccineeApi.add(form.value)
    
    if (res.code === 200) {
      message.success(form.value.id ? '更新成功' : '添加成功')
      showModal.value = false
      resetForm()
      loadVaccinees()
    } else {
      message.error(res.message || '操作失败')
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    userId: null,
    realName: '',
    idCard: '',
    gender: 1,
    birthDate: null,
    phone: '',
    isDefault: false
  }
  birthDateTs.value = null
}

onMounted(() => {
  loadVaccinees()
})
</script>
