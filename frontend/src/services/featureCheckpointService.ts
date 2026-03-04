import api from './api';
import { ApiResponse, FeatureCheckpoint, FeatureCheckpointUpdateRequest } from '../types';

export const featureCheckpointService = {
  getCheckpoints: async (featureId: string): Promise<FeatureCheckpoint[]> => {
    const response = await api.get<ApiResponse<FeatureCheckpoint[]>>(`/features/${featureId}/checkpoints`);
    return response.data.data;
  },

  updateCheckpoint: async (featureId: string, checkpointId: string, data: FeatureCheckpointUpdateRequest): Promise<FeatureCheckpoint> => {
    const response = await api.patch<ApiResponse<FeatureCheckpoint>>(`/features/${featureId}/checkpoints/${checkpointId}`, data);
    return response.data.data;
  },

  getStats: async (featureId: string): Promise<Record<string, number>> => {
    const response = await api.get<ApiResponse<Record<string, number>>>(`/features/${featureId}/checkpoints/stats`);
    return response.data.data;
  },

  getProgress: async (featureId: string): Promise<number> => {
    const response = await api.get<ApiResponse<number>>(`/features/${featureId}/checkpoints/progress`);
    return response.data.data;
  },
};
