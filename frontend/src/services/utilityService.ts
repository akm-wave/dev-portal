import api from './api';
import { ApiResponse, PagedResponse, Utility, UtilityRequest, UtilityAttachment, UtilityType } from '../types';

interface GetUtilitiesParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  type?: UtilityType;
  search?: string;
}

export const utilityService = {
  getAll: async (params: GetUtilitiesParams = {}): Promise<PagedResponse<Utility>> => {
    const response = await api.get<ApiResponse<PagedResponse<Utility>>>('/utilities', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Utility> => {
    const response = await api.get<ApiResponse<Utility>>(`/utilities/${id}`);
    return response.data.data;
  },

  create: async (data: UtilityRequest): Promise<Utility> => {
    const response = await api.post<ApiResponse<Utility>>('/utilities', data);
    return response.data.data;
  },

  update: async (id: string, data: UtilityRequest): Promise<Utility> => {
    const response = await api.put<ApiResponse<Utility>>(`/utilities/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/utilities/${id}`);
  },

  getAttachments: async (id: string): Promise<UtilityAttachment[]> => {
    const response = await api.get<ApiResponse<UtilityAttachment[]>>(`/utilities/${id}/attachments`);
    return response.data.data;
  },

  uploadAttachment: async (id: string, file: File): Promise<UtilityAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<UtilityAttachment>>(`/utilities/${id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },

  deleteAttachment: async (utilityId: string, attachmentId: string): Promise<void> => {
    await api.delete(`/utilities/${utilityId}/attachments/${attachmentId}`);
  },
};
