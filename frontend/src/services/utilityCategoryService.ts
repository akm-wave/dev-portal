import api from './api';
import { ApiResponse, UtilityCategory, UtilityCategoryRequest } from '../types';

export const utilityCategoryService = {
  getAll: async (): Promise<UtilityCategory[]> => {
    const response = await api.get<ApiResponse<UtilityCategory[]>>('/utility-categories');
    return response.data.data;
  },

  getTree: async (): Promise<UtilityCategory[]> => {
    const response = await api.get<ApiResponse<UtilityCategory[]>>('/utility-categories/tree');
    return response.data.data;
  },

  getById: async (id: string): Promise<UtilityCategory> => {
    const response = await api.get<ApiResponse<UtilityCategory>>(`/utility-categories/${id}`);
    return response.data.data;
  },

  create: async (request: UtilityCategoryRequest): Promise<UtilityCategory> => {
    const response = await api.post<ApiResponse<UtilityCategory>>('/utility-categories', request);
    return response.data.data;
  },

  update: async (id: string, request: UtilityCategoryRequest): Promise<UtilityCategory> => {
    const response = await api.put<ApiResponse<UtilityCategory>>(`/utility-categories/${id}`, request);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/utility-categories/${id}`);
  },
};

export default utilityCategoryService;
