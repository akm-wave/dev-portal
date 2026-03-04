import api from './api';
import { ApiResponse, DashboardStats } from '../types';

export const dashboardService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get<ApiResponse<DashboardStats>>('/dashboard');
    return response.data.data;
  },
};
