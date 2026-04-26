import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('admin_token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

export async function adminLogin(username, password) {
    const { data } = await api.post('/auth/login', { username, password })
    return data
}

// 需要后端提供：GET /api/admin/items
export async function adminListItems(params = {}) {
    const { data } = await api.get('/admin/items', { params })
    return data
}

export async function adminMarkSold(id) {
    const { data } = await api.put(`/admin/items/${id}/sold`)
    return data
}

export async function adminDeleteItem(id) {
    const { data } = await api.delete(`/admin/items/${id}`)
    return data
}