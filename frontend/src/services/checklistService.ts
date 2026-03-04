import api from './api';
import { ApiResponse, PagedResponse, Checklist, ChecklistRequest, ChecklistStatus, ChecklistPriority } from '../types';

interface GetChecklistsParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  status?: ChecklistStatus;
  priority?: ChecklistPriority;
  search?: string;
}

export const checklistService = {
  getAll: async (params: GetChecklistsParams = {}): Promise<PagedResponse<Checklist>> => {
    const response = await api.get<ApiResponse<PagedResponse<Checklist>>>('/checklists', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Checklist> => {
    const response = await api.get<ApiResponse<Checklist>>(`/checklists/${id}`);
    return response.data.data;
  },

  create: async (data: ChecklistRequest): Promise<Checklist> => {
    const response = await api.post<ApiResponse<Checklist>>('/checklists', data);
    return response.data.data;
  },

  update: async (id: string, data: ChecklistRequest): Promise<Checklist> => {
    const response = await api.put<ApiResponse<Checklist>>(`/checklists/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/checklists/${id}`);
  },

  updateStatus: async (id: string, status: ChecklistStatus): Promise<Checklist> => {
    const response = await api.patch<ApiResponse<Checklist>>(`/checklists/${id}/status`, null, {
      params: { status },
    });
    return response.data.data;
  },
};
