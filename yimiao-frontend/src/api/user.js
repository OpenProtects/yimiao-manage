import request from './request'

export const userApi = {
  login(data) {
    return request.post('/user/login', data)
  },

  register(data) {
    return request.post('/user/register', data)
  },

  logout() {
    return request.post('/user/logout')
  },

  getUserInfo() {
    return request.get('/user/info')
  },

  updatePassword(oldPassword, newPassword) {
    return request.put('/user/password', null, {
      params: { oldPassword, newPassword }
    })
  },

  sendSms(phone) {
    return request.post('/sms/send', null, { params: { phone } })
  }
}

export const vaccineeApi = {
  list() {
    return request.get('/vaccinee/list')
  },

  getDetail(id) {
    return request.get(`/vaccinee/${id}`)
  },

  add(data) {
    return request.post('/vaccinee', data)
  },

  update(data) {
    return request.put('/vaccinee', data)
  },

  delete(id) {
    return request.delete(`/vaccinee/${id}`)
  },

  setDefault(id) {
    return request.put(`/vaccinee/${id}/default`)
  }
}

export const certApi = {
  apply(data) {
    return request.post('/cert/apply', data)
  },

  getStatus() {
    return request.get('/cert/status')
  },

  checkCertified() {
    return request.get('/cert/check')
  }
}
