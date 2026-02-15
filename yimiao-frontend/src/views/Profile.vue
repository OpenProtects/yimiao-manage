<template>
  <div class="page-container">
    <n-card title="个人中心">
      <n-descriptions :column="2" bordered>
        <n-descriptions-item label="用户ID">{{ userStore.userInfo?.userId }}</n-descriptions-item>
        <n-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</n-descriptions-item>
        <n-descriptions-item label="用户类型">
          {{ userStore.userType === 1 ? '管理员' : '普通用户' }}
        </n-descriptions-item>
      </n-descriptions>
      
      <n-divider />
      
      <n-h3>修改密码</n-h3>
      <n-form ref="formRef" :model="form" :rules="rules" style="max-width: 400px">
        <n-form-item path="oldPassword" label="原密码">
          <n-input
            v-model:value="form.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item path="newPassword" label="新密码">
          <n-input
            v-model:value="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item path="confirmPassword" label="确认密码">
          <n-input
            v-model:value="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" :loading="loading" @click="handleChangePassword">
            修改密码
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const message = useMessage()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: { required: true, message: '请输入原密码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === form.value.newPassword
      },
      message: '两次密码不一致',
      trigger: 'blur'
    }
  ]
}

const handleChangePassword = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const res = await userApi.updatePassword(form.value.oldPassword, form.value.newPassword)
    if (res.code === 200) {
      message.success('密码修改成功，请重新登录')
      userStore.logout()
      window.location.href = '/login'
    } else {
      message.error(res.message || '修改失败')
    }
  } finally {
    loading.value = false
  }
}
</script>
