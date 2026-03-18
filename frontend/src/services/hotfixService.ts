import api from './api';
import { ApiResponse, PagedResponse, Hotfix, HotfixRequest, ChecklistProgress, ChecklistProgressUpdateRequest } from '../types';

export const hotfixService = {
  getAll: async (params?: { page?: number; size?: number; status?: string; search?: string }): Promise<PagedResponse<Hotfix>> => {
    const response = await api.get<ApiResponse<PagedResponse<Hotfix>>>('/hotfixes', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Hotfix> => {
    const response = await api.get<ApiResponse<Hotfix>>(`/hotfixes/${id}`);
    return response.data.data;
  },

  getDetails: async (id: string): Promise<any> => {
    const response = await api.get<ApiResponse<any>>(`/hotfixes/${id}/details`);
    return response.data.data;
  },

  create: async (data: HotfixRequest): Promise<Hotfix> => {
    const response = await api.post<ApiResponse<Hotfix>>('/hotfixes', data);
    return response.data.data;
  },

  update: async (id: string, data: HotfixRequest): Promise<Hotfix> => {
    const response = await api.put<ApiResponse<Hotfix>>(`/hotfixes/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/hotfixes/${id}`);
  },

  // Checklist Progress
  getChecklistProgress: async (hotfixId: string): Promise<ChecklistProgress[]> => {
    const response = await api.get<ApiResponse<ChecklistProgress[]>>(`/hotfixes/${hotfixId}/checklists`);
    return response.data.data;
  },

  updateChecklistStatus: async (hotfixId: string, checklistId: string, data: ChecklistProgressUpdateRequest): Promise<ChecklistProgress> => {
    const response = await api.put<ApiResponse<ChecklistProgress>>(`/hotfixes/${hotfixId}/checklists/${checklistId}`, data);
    return response.data.data;
  },

  uploadChecklistAttachment: async (hotfixId: string, checklistId: string, file: File): Promise<ChecklistProgress> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<ChecklistProgress>>(`/hotfixes/${hotfixId}/checklists/${checklistId}/attachment`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },

  downloadChecklistAttachment: async (hotfixId: string, checklistId: string): Promise<Blob> => {
    const response = await api.get(`/hotfixes/${hotfixId}/checklists/${checklistId}/attachment`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteChecklistAttachment: async (hotfixId: string, checklistId: string): Promise<void> => {
    await api.delete(`/hotfixes/${hotfixId}/checklists/${checklistId}/attachment`);
  },
};
