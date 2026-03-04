import api from './api';
import { ApiResponse, PagedResponse } from '../types';

export interface AuditLog {
  id: string;
  username: string;
  action: string;
  entityType: string;
  entityId: string;
  description: string;
  ipAddress: string | null;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
}

export const auditService = {
  getAll: async (params?: { 
    page?: number; 
    size?: number; 
    entityType?: string; 
    action?: string; 
    userId?: string;
  }): Promise<PagedResponse<AuditLog>> => {
    const response = await api.get<ApiResponse<PagedResponse<AuditLog>>>('/audit', { params });
    return response.data.data;
  },

  getEntityTypes: async (): Promise<string[]> => {
    const response = await api.get<ApiResponse<string[]>>('/audit/entity-types');
    return response.data.data;
  },

  getActions: async (): Promise<string[]> => {
    const response = await api.get<ApiResponse<string[]>>('/audit/actions');
    return response.data.data;
  },
};
