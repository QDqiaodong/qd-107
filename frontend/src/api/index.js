import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export const userApi = {
  register: (data) => request.post('/user/register', data),
  login: (data) => request.post('/user/login', data),
  getInfo: (id) => request.get(`/user/${id}`),
  update: (data) => request.put('/user', data)
}

export const sportTypeApi = {
  getHotTypes: () => request.get('/sport-type/hot'),
  getList: () => request.get('/sport-type/list')
}

export const checkinApi = {
  getList: (userId, page, size) => request.get('/checkin/list', { params: { userId, page, size } }),
  getDetail: (id) => request.get(`/checkin/${id}`),
  create: (data) => request.post('/checkin', data)
}

export const planApi = {
  getList: (userId) => request.get('/plan/list', { params: { userId } }),
  getDetail: (id) => request.get(`/plan/${id}`),
  create: (data) => request.post('/plan', data),
  update: (data) => request.put('/plan', data),
  delete: (id) => request.delete(`/plan/${id}`),
  setReminder: (data) => request.post('/plan/reminder', data),
  getExecutionSnapshots: (userId, status) => request.get('/plan/execution-snapshots', { params: { userId, status } })
}

export const dynamicApi = {
  getPublicList: (page, size) => request.get('/dynamic/public', { params: { page, size } }),
  getByType: (typeId, page, size) => request.get(`/dynamic/type/${typeId}`, { params: { page, size } }),
  getMyList: (userId) => request.get('/dynamic/my', { params: { userId } }),
  getDetail: (id) => request.get(`/dynamic/${id}`),
  create: (data) => request.post('/dynamic', data),
  delete: (id) => request.delete(`/dynamic/${id}`),
  like: (data) => request.post('/dynamic/like', data)
}

export const statsApi = {
  getWeekly: (userId) => request.get('/statistics/week', { params: { userId } }),
  getMonthly: (userId) => request.get('/statistics/month', { params: { userId } }),
  getIntensity: (userId, period) => request.get('/statistics/intensity', { params: { userId, period } })
}

export default request
