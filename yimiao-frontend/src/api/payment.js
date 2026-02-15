import request from './request'

export const paymentApi = {
  getChannelList() {
    return request.get('/payment/channel/list')
  },
  
  getChannel(id) {
    return request.get(`/payment/channel/${id}`)
  },
  
  enableChannel(id) {
    return request.post(`/payment/channel/enable/${id}`)
  },
  
  disableChannel(id) {
    return request.post(`/payment/channel/disable/${id}`)
  },
  
  updateChannel(data) {
    return request.post('/payment/channel/update', data)
  },
  
  getEnabledChannels() {
    return request.get('/payment/channels')
  },
  
  createPayment(data) {
    return request.post('/payment/create', data)
  },
  
  getPayUrl(tradeNo, subject = '疫苗预约') {
    return request.get(`/payment/pay-url/${tradeNo}`, { params: { subject } })
  },
  
  queryStatus(orderNo) {
    return request.get(`/payment/status/${orderNo}`)
  },
  
  closePayment(orderNo) {
    return request.post(`/payment/close/${orderNo}`)
  },
  
  getRecord(orderNo) {
    return request.get(`/payment/record/${orderNo}`)
  }
}
