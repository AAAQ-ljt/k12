import request from '@/api/request'

export const loadLearningAnalysisDashboard = (params) =>
  request.post('/learningAnalysis/loadDashboard', params, {
    showLoading: false,
  })
