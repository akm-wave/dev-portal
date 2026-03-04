import api from './api';
import { ApiResponse, Template, TemplateRequest, TemplateEntityType } from '../types';

export const templateService = {
  getAll: async (): Promise<Template[]> => {
    const response = await api.get<ApiResponse<Template[]>>('/templates');
    return response.data.data;
  },

  getByType: async (entityType: TemplateEntityType): Promise<Template[]> => {
    const response = await api.get<ApiResponse<Template[]>>(`/templates/type/${entityType}`);
    return response.data.data;
  },

  getById: async (id: string): Promise<Template> => {
    const response = await api.get<ApiResponse<Template>>(`/templates/${id}`);
    return response.data.data;
  },

  getDefault: async (entityType: TemplateEntityType): Promise<Template | null> => {
    const response = await api.get<ApiResponse<Template>>(`/templates/default/${entityType}`);
    return response.data.data;
  },

  create: async (request: TemplateRequest): Promise<Template> => {
    const response = await api.post<ApiResponse<Template>>('/templates', request);
    return response.data.data;
  },

  update: async (id: string, request: TemplateRequest): Promise<Template> => {
    const response = await api.put<ApiResponse<Template>>(`/templates/${id}`, request);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/templates/${id}`);
  },
};

export default templateService;
