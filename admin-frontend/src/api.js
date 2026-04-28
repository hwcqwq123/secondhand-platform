import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const msg = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export async function adminLogin(username, password) {
  const { data } = await api.post('/auth/login', { username, password })
  return data
}

// 新增：管理员注册接口，对应后端 POST /api/auth/admin/register
export async function adminRegister(payload) {
  const { data } = await api.post('/auth/admin/register', payload)
  return data
}

export async function adminStats() {
  const { data } = await api.get('/admin/stats')
  return data
}

export async function adminListItems(params = {}) {
  const { data } = await api.get('/admin/items', { params })
  return data
}

export async function adminMarkSold(id, reason) {
  const { data } = await api.put(`/admin/items/${id}/sold`, { reason })
  return data
}

export async function adminOffShelfItem(id, reason) {
  const { data } = await api.put(`/admin/items/${id}/off-shelf`, { reason })
  return data
}

export async function adminDeleteItem(id, reason) {
  const { data } = await api.delete(`/admin/items/${id}`, {
    data: { reason }
  })
  return data
}

export async function adminListOrders(params = {}) {
  const { data } = await api.get('/admin/orders', { params })
  return data
}

export async function adminUpdateOrderStatus(id, status, reason) {
  const { data } = await api.put(`/admin/orders/${id}/status`, { status, reason })
  return data
}

export async function adminListUsers(params = {}) {
  const { data } = await api.get('/admin/users', { params })
  return data
}

export async function adminUpdateUser(id, payload) {
  const { data } = await api.put(`/admin/users/${id}`, payload)
  return data
}

export async function adminListLogs(params = {}) {
  const { data } = await api.get('/admin/logs', { params })
  return data
}