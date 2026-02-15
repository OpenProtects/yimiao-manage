<template>
  <div class="login-container">
    <n-card class="login-card" title="用户注册">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="form.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item path="phone" label="手机号">
          <n-input v-model:value="form.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item path="smsCode" label="验证码">
          <n-input-group>
            <n-input v-model:value="form.smsCode" placeholder="请输入验证码" />
            <n-button
              :disabled="countdown > 0"
              :loading="sending"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </n-button>
          </n-input-group>
        </n-form-item>
        <n-form-item>
          <n-button type="primary" block :loading="loading" @click="handleRegister">
            注册
          </n-button>
        </n-form-item>
        <n-form-item>
          <n-button text @click="router.push('/login')">
            已有账号？立即登录
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { userApi } from '@/api/user'

const router = useRouter()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const form = ref({
  username: '',
  password: '',
  phone: '',
  smsCode: ''
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: { required: true, message: '请输入验证码', trigger: 'blur' }
}

const sendCode = async () => {
  if (!form.value.phone || !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    message.error('请输入正确的手机号')
    return
  }
  
  sending.value = true
  try {
    const res = await userApi.sendSms(form.value.phone)
    if (res.code === 200) {
      message.success('验证码已发送')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      message.error(res.message || '发送失败')
    }
  } finally {
    sending.value = false
  }
}

const handleRegister = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const res = await userApi.register(form.value)
    if (res.code === 200) {
      message.success('注册成功')
      router.push('/login')
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-card {
  width: 400px;
  max-width: 95vw;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

@media (max-width: 480px) {
  .login-card {
    width: 95vw;
    margin: 16px;
  }
}
</style>
