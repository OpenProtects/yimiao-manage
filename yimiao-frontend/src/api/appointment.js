import request from './request'

export const appointmentApi = {
  create(data) {
    return request.post('/appointment', data)
  },

  cancel(id, reason) {
    return request.post(`/appointment/cancel/${id}`, null, {
      params: { reason }
    })
  },

  getDetail(id) {
    return request.get(`/appointment/${id}`)
  },

  getPage(params) {
    return request.get('/appointment/page', { params })
  },

  getMyAppointments() {
    return request.get('/appointment/my')
  },

  verify(id) {
    return request.post(`/appointment/verify/${id}`)
  },

  getByOrderNo(orderNo) {
    return request.get(`/appointment/order-no/${orderNo}`)
  }
}

export const blacklistApi = {
  check(idCard) {
    return request.get(`/blacklist/check/${idCard}`)
  },

  add(idCard, realName, reason, type) {
    return request.post('/blacklist/add', null, {
      params: { idCard, realName, reason, type }
    })
  },

  remove(idCard) {
    return request.post(`/blacklist/remove/${idCard}`)
  },

  getDetail(idCard) {
    return request.get(`/blacklist/${idCard}`)
  }
}

export const paymentApi = {
  create(data) {
    return request.post('/payment/create', data)
  },

  notify(tradeNo, orderNo, success) {
    return request.post('/payment/notify', null, {
      params: { tradeNo, orderNo, success }
    })
  },

  queryStatus(orderNo) {
    return request.get(`/payment/status/${orderNo}`)
  },

  close(orderNo) {
    return request.post(`/payment/close/${orderNo}`)
  },

  getRecord(orderNo) {
    return request.get(`/payment/record/${orderNo}`)
  }
}

export const refundApi = {
  create(orderId, orderNo, userId, amount, reason) {
    return request.post('/refund/create', null, {
      params: { orderId, orderNo, userId, amount, reason }
    })
  },

  notify(refundNo, success) {
    return request.post('/refund/notify', null, {
      params: { refundNo, success }
    })
  },

  getRecord(orderNo) {
    return request.get(`/refund/record/${orderNo}`)
  }
}

export const statisticsApi = {
  getOverview() {
    return request.get('/statistics/overview')
  },

  getDaily(date) {
    return request.get('/statistics/daily', { params: { date } })
  },

  getAppointment(startDate, endDate) {
    return request.get('/statistics/appointment', {
      params: { startDate, endDate }
    })
  },

  getVaccine() {
    return request.get('/statistics/vaccine')
  },

  getSite(siteId) {
    return request.get(`/statistics/site/${siteId}`)
  }
}
