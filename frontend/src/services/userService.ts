import api from './api';
import { ApiResponse, PagedResponse, User, UserSummary } from '../types';

export const userService = {
  getAll: async (params?: { page?: number; size?: number; approved?: boolean }): Promise<PagedResponse<User>> => {
    const response = await api.get<ApiResponse<PagedResponse<User>>>('/users', { params });
    return response.data.data;
  },

  getApprovedUsers: async (): Promise<UserSummary[]> => {
    const response = await api.get<ApiResponse<UserSummary[]>>('/users/approved');
    return response.data.data;
  },

  getById: async (id: string): Promise<User> => {
    const response = await api.get<ApiResponse<User>>(`/users/${id}`);
    return response.data.data;
  },

  approve: async (id: string): Promise<User> => {
    const response = await api.patch<ApiResponse<User>>(`/users/${id}/approve`);
    return response.data.data;
  },

  reject: async (id: string): Promise<User> => {
    const response = await api.patch<ApiResponse<User>>(`/users/${id}/reject`);
    return response.data.data;
  },
};
