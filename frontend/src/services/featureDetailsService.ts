import api from './api';
import { ApiResponse, FeatureDetails, CheckpointAnalysis, CheckpointProgressRequest, FeatureCheckpoint } from '../types';

export const featureDetailsService = {
  getFeatureDetails: async (featureId: string): Promise<FeatureDetails> => {
    const response = await api.get<ApiResponse<FeatureDetails>>(`/features/${featureId}/details`);
    return response.data.data;
  },

  getUniqueCheckpoints: async (featureId: string): Promise<CheckpointAnalysis[]> => {
    const response = await api.get<ApiResponse<CheckpointAnalysis[]>>(`/features/${featureId}/checkpoints/unique`);
    return response.data.data;
  },

  // Use the new FeatureCheckpointController endpoint
  updateCheckpointProgress: async (
    featureId: string,
    checkpointId: string,
    request: CheckpointProgressRequest
  ): Promise<FeatureCheckpoint> => {
    const response = await api.patch<ApiResponse<FeatureCheckpoint>>(
      `/features/${featureId}/checkpoints/${checkpointId}`,
      request
    );
    return response.data.data;
  },

  linkCheckpoints: async (featureId: string, checklistIds: string[]): Promise<void> => {
    await api.post(`/features/${featureId}/checkpoints/link`, checklistIds);
  },

  uploadCheckpointAttachment: async (featureId: string, checkpointId: string, file: File): Promise<FeatureCheckpoint> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<FeatureCheckpoint>>(
      `/features/${featureId}/checkpoints/${checkpointId}/attachment`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.data;
  },

  downloadCheckpointAttachment: async (featureId: string, checkpointId: string): Promise<Blob> => {
    const response = await api.get(`/features/${featureId}/checkpoints/${checkpointId}/attachment`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteCheckpointAttachment: async (featureId: string, checkpointId: string): Promise<void> => {
    await api.delete(`/features/${featureId}/checkpoints/${checkpointId}/attachment`);
  },
};
