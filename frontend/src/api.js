import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

export async function register(username, password) {
    const { data } = await api.post('/auth/register', { username, password })
    return data
}

export async function login(username, password) {
    const { data } = await api.post('/auth/login', { username, password })
    return data
}

export async function listItems(params = {}) {
    const { data } = await api.get('/items', { params })
    return data
}

export async function getItemDetail(id) {
    const { data } = await api.get(`/items/${id}`)
    return data
}

export async function createItem(payload) {
    const { data } = await api.post('/items', payload)
    return data
}

export async function deleteItem(id) {
    const { data } = await api.delete(`/items/${id}`)
    return data
}

export async function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)

    const { data } = await api.post('/files/upload', formData)
    return data
}


export async function createOrder(itemId) {
    const { data } = await api.post('/orders', { itemId })
    return data
}

export async function listMyOrders() {
    const { data } = await api.get('/orders/my')
    return data
}

export async function payOrder(orderId) {
    const { data } = await api.put(`/orders/${orderId}/pay`)
    return data
}

export async function cancelOrder(orderId) {
    const { data } = await api.put(`/orders/${orderId}/cancel`)
    return data
}

export async function getRecommendations(limit = 10) {
    const { data } = await api.get('/recommendations', {
        params: { limit, t: Date.now() }
    })
    return data
}

export async function getMyProfile() {
    const { data } = await api.get('/users/me')
    return data
}

export async function updateMyProfile(payload) {
    const { data } = await api.put('/users/me', payload)
    return data
}

export async function updateMyPayment(payload) {
    const { data } = await api.put('/users/me/payment', payload)
    return data
}

export async function listMySalesOrders() {
    const { data } = await api.get('/orders/my-sales')
    return data
}

export async function listMyItems() {
    const { data } = await api.get('/items/my')
    return data
}

export async function updateItem(id, payload) {
    const { data } = await api.put(`/items/${id}`, payload)
    return data
}

export async function offShelfItem(id) {
    const { data } = await api.put(`/items/${id}/off-shelf`)
    return data
}

export async function putOnShelfItem(id) {
    const { data } = await api.put(`/items/${id}/put-on-shelf`)
    return data
}