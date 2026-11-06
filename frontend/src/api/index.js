import request from './request'

/* ================= 认证 ================= */
export const login = (data) => request.post('/api/auth/login', data)
export const register = (data) => request.post('/api/auth/register', data)
export const logout = () => request.post('/api/auth/logout')
export const me = () => request.get('/api/auth/me')

/* ================= 稿件(作者) ================= */
export const submitManuscript = (formData) =>
  request.post('/api/manuscripts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const myManuscripts = () => request.get('/api/manuscripts/mine')
export const manuscriptLogs = (id) => request.get(`/api/manuscripts/${id}/logs`)
export const reviseManuscript = (id, formData) =>
  request.post(`/api/manuscripts/${id}/revisions`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

/* ================= 审稿人 ================= */
export const myReviewTasks = () => request.get('/api/reviews/mine')
export const submitOpinion = (assignmentId, data) =>
  request.post(`/api/reviews/${assignmentId}/opinion`, data)

/* ================= 管理员 ================= */
export const adminStats = () => request.get('/api/admin/stats')
export const pendingPool = () => request.get('/api/admin/manuscripts/pending')
export const reviewingPool = () => request.get('/api/admin/manuscripts/reviewing')
export const acceptedPool = () => request.get('/api/admin/manuscripts/accepted')
export const reviewerList = () => request.get('/api/admin/reviewers')
export const passInitial = (id, reviewerIds) =>
  request.post(`/api/admin/manuscripts/${id}/pass-initial`, { reviewerIds })
export const rejectInitial = (id) => request.post(`/api/admin/manuscripts/${id}/reject-initial`)
export const assignReviewers = (id, reviewerIds) =>
  request.post(`/api/admin/manuscripts/${id}/assign`, { reviewerIds })
export const manuscriptOpinions = (id) => request.get(`/api/admin/manuscripts/${id}/opinions`)
export const finalReview = (id, decision) =>
  request.post(`/api/admin/manuscripts/${id}/final`, { decision })

/* ================= 评奖 ================= */
export const awardList = () => request.get('/api/awards')
export const createAward = (data) => request.post('/api/awards', data)
export const startAwardReview = (id) => request.post(`/api/awards/${id}/start`)
export const nominate = (awardId, manuscriptId) =>
  request.post(`/api/awards/${awardId}/nominations`, { manuscriptId })
export const awardNominations = (awardId) => request.get(`/api/awards/${awardId}/nominations`)
export const myAwardScores = (awardId) => request.get(`/api/awards/${awardId}/myscores`)
export const scoreNomination = (awardId, nominationId, score) =>
  request.post(`/api/awards/${awardId}/nominations/${nominationId}/score`, { score })
export const publishAward = (awardId, winnerCount) =>
  request.post(`/api/awards/${awardId}/publish?winnerCount=${winnerCount}`)
