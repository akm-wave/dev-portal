import api from './api';
import { ApiResponse, PagedResponse, Feature, FeatureRequest, FeatureStatus, Microservice, Checklist } from '../types';

interface GetFeaturesParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  status?: FeatureStatus;
  search?: string;
  assignedToId?: string;
}

export const featureService = {
  getAll: async (params: GetFeaturesParams = {}): Promise<PagedResponse<Feature>> => {
    const response = await api.get<ApiResponse<PagedResponse<Feature>>>('/features', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Feature> => {
    const response = await api.get<ApiResponse<Feature>>(`/features/${id}`);
    return response.data.data;
  },

  create: async (data: FeatureRequest): Promise<Feature> => {
    const response = await api.post<ApiResponse<Feature>>('/features', data);
    return response.data.data;
  },

  update: async (id: string, data: FeatureRequest): Promise<Feature> => {
    const response = await api.put<ApiResponse<Feature>>(`/features/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/features/${id}`);
  },

  getImpactedMicroservices: async (id: string): Promise<Microservice[]> => {
    const response = await api.get<ApiResponse<Microservice[]>>(`/features/${id}/microservices`);
    return response.data.data;
  },

  getAggregatedChecklists: async (id: string): Promise<Checklist[]> => {
    const response = await api.get<ApiResponse<Checklist[]>>(`/features/${id}/checklists`);
    return response.data.data;
  },
};
