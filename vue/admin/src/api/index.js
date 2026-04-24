import request from './request'

export const authAPI = {
  login: (data) => request.post('/auth/admin/login', data),
  logout: () => request.post('/auth/logout'),
  getAdminInfo: () => request.get('/auth/admin/info')
}

export const userAPI = {
  list: (params) => request.get('/admin/users', { params }),
  detail: (id) => request.get(`/admin/users/${id}`),
  update: (id, data) => request.put(`/admin/users/${id}`, data),
  delete: (id) => request.delete(`/admin/users/${id}`),
  disable: (id) => request.put(`/admin/users/${id}/disable`)
}

export const postAPI = {
  list: (params) => request.get('/admin/posts', { params }),
  detail: (id) => request.get(`/admin/posts/${id}`),
  delete: (id) => request.delete(`/admin/posts/${id}`),
  recommend: (id) => request.put(`/admin/posts/${id}/recommend`)
}

export const commentAPI = {
  list: (params) => request.get('/admin/comments', { params }),
  delete: (id) => request.delete(`/admin/comments/${id}`)
}

export const notificationAPI = {
  list: (params) => request.get('/admin/notifications', { params }),
  create: (data) => request.post('/admin/notifications', data),
  delete: (id) => request.delete(`/admin/notifications/${id}`)
}

export const statsAPI = {
  dashboard: () => request.get('/admin/stats/dashboard'),
  userTrend: (params) => request.get('/admin/stats/user-trend', { params }),
  postTrend: (params) => request.get('/admin/stats/post-trend', { params }),
  tradeDistribution: () => request.get('/admin/stats/trade-distribution')
}