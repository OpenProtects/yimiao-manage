import request from './request'

export const vaccineApi = {
  getPage(params) {
    return request.get('/vaccine/page', { params })
  },

  getDetail(id) {
    return request.get(`/vaccine/${id}`)
  },

  getAvailable() {
    return request.get('/vaccine/available')
  },

  add(data) {
    return request.post('/vaccine', data)
  },

  update(data) {
    return request.put('/vaccine', data)
  },

  delete(id) {
    return request.delete(`/vaccine/${id}`)
  },

  checkAge(vaccineId, age) {
    return request.get('/vaccine/check-age', { params: { vaccineId, age } })
  }
}

export const siteApi = {
  getPage(params) {
    return request.get('/site/page', { params })
  },

  getDetail(id) {
    return request.get(`/site/${id}`)
  },

  getAll() {
    return request.get('/site/all')
  },

  add(data) {
    return request.post('/site', data)
  },

  update(data) {
    return request.put('/site', data)
  },

  delete(id) {
    return request.delete(`/site/${id}`)
  }
}

export const slotApi = {
  getPage(params) {
    return request.get('/slot/page', { params })
  },

  getAvailable(siteId, vaccineId, date) {
    return request.get('/slot/available', { params: { siteId, vaccineId, date } })
  },

  getDetail(id) {
    return request.get(`/slot/${id}`)
  },

  add(data) {
    return request.post('/slot', data)
  },

  update(data) {
    return request.put('/slot', data)
  },

  delete(id) {
    return request.delete(`/slot/${id}`)
  },

  book(id) {
    return request.post(`/slot/book/${id}`)
  },

  cancel(id) {
    return request.post(`/slot/cancel/${id}`)
  },

  generate(siteId, vaccineId, startDate, endDate, dailyCount) {
    return request.post('/slot/generate', null, {
      params: { siteId, vaccineId, startDate, endDate, dailyCount }
    })
  }
}

export const stockApi = {
  getAvailable(siteId, vaccineId) {
    return request.get('/stock/available', { params: { siteId, vaccineId } })
  },

  deduct(siteId, vaccineId, quantity) {
    return request.post('/stock/deduct', null, {
      params: { siteId, vaccineId, quantity }
    })
  },

  add(siteId, vaccineId, quantity) {
    return request.post('/stock/add', null, {
      params: { siteId, vaccineId, quantity }
    })
  },

  listBySite(siteId) {
    return request.get(`/stock/list/${siteId}`)
  }
}
