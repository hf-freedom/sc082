import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

api.interceptors.response.use(
  (response) => {
    const { data } = response
    if (data.code === 200) {
      return data.data
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const caseApi = {
  list: () => api.get('/cases'),
  getById: (id) => api.get(`/cases/${id}`),
  getByNumber: (caseNumber) => api.get(`/cases/number/${caseNumber}`),
  getByStatus: (status) => api.get(`/cases/status/${status}`),
  submit: (caseData) => api.post('/cases/submit', caseData),
  accept: (id, handler) => api.post(`/cases/${id}/accept`, { handler }),
  supplement: (id, materials) => api.post(`/cases/${id}/supplement`, materials),
  approve: (id, data) => api.post(`/cases/${id}/approve`, data),
  withdraw: (id, reason) => api.post(`/cases/${id}/withdraw`, { reason }),
  getRequiredMaterials: (itemId) => api.get(`/cases/${itemId}/materials/required`),
  getOptionalMaterials: (itemId) => api.get(`/cases/${itemId}/materials/optional`),
}

export const itemApi = {
  list: () => api.get('/items'),
  listActive: () => api.get('/items/active'),
  getById: (id) => api.get(`/items/${id}`),
}

export const materialApi = {
  list: () => api.get('/materials'),
  listRequired: () => api.get('/materials/required'),
}

export const supervisionApi = {
  listRecords: () => api.get('/supervision/records'),
  getRecordsByCaseId: (caseId) => api.get(`/supervision/records/case/${caseId}`),
  listOverdue: () => api.get('/supervision/overdue'),
  check: () => api.post('/supervision/check'),
  forceOverdue: (caseId) => api.post(`/supervision/force-overdue/${caseId}`),
}

export const reportApi = {
  list: () => api.get('/reports'),
  getByDate: (date) => api.get(`/reports/date/${date}`),
  getByRange: (start, end) => api.get(`/reports/range?start=${start}&end=${end}`),
  generate: (date) => api.post(`/reports/generate/${date}`),
}

export const documentApi = {
  getById: (id) => api.get(`/documents/${id}`),
  getByCaseId: (caseId) => api.get(`/documents/case/${caseId}`),
  generate: (data) => api.post('/documents/generate', data),
}

export default api
