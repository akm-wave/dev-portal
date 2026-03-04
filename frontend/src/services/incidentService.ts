import api from './api';
import { ApiResponse, PagedResponse, Incident, IncidentRequest, ChecklistProgress, ChecklistProgressUpdateRequest } from '../types';

export const incidentService = {
  getAll: async (params?: { page?: number; size?: number }): Promise<PagedResponse<Incident>> => {
    const response = await api.get<ApiResponse<PagedResponse<Incident>>>('/incidents', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Incident> => {
    const response = await api.get<ApiResponse<Incident>>(`/incidents/${id}`);
    return response.data.data;
  },

  getDetails: async (id: string): Promise<any> => {
    const response = await api.get<ApiResponse<any>>(`/incidents/${id}/details`);
    return response.data.data;
  },

  create: async (data: IncidentRequest): Promise<Incident> => {
    const response = await api.post<ApiResponse<Incident>>('/incidents', data);
    return response.data.data;
  },

  update: async (id: string, data: IncidentRequest): Promise<Incident> => {
    const response = await api.put<ApiResponse<Incident>>(`/incidents/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/incidents/${id}`);
  },

  // Checklist Progress
  getChecklistProgress: async (incidentId: string): Promise<ChecklistProgress[]> => {
    const response = await api.get<ApiResponse<ChecklistProgress[]>>(`/incidents/${incidentId}/checklists`);
    return response.data.data;
  },

  updateChecklistStatus: async (incidentId: string, checklistId: string, data: ChecklistProgressUpdateRequest): Promise<ChecklistProgress> => {
    const response = await api.put<ApiResponse<ChecklistProgress>>(`/incidents/${incidentId}/checklists/${checklistId}`, data);
    return response.data.data;
  },

  uploadChecklistAttachment: async (incidentId: string, checklistId: string, file: File): Promise<ChecklistProgress> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<ChecklistProgress>>(`/incidents/${incidentId}/checklists/${checklistId}/attachment`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },

  downloadChecklistAttachment: async (incidentId: string, checklistId: string): Promise<Blob> => {
    const response = await api.get(`/incidents/${incidentId}/checklists/${checklistId}/attachment`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteChecklistAttachment: async (incidentId: string, checklistId: string): Promise<void> => {
    await api.delete(`/incidents/${incidentId}/checklists/${checklistId}/attachment`);
  },
};
