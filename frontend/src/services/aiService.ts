import api from './api';
import { ApiResponse, AiSummary, SimilaritySuggestion, ReleaseRecommendation, SummaryType } from '../types';

export const aiService = {
  // Smart Summaries
  generateSummary: async (entityType: string, entityId: string, summaryType: SummaryType): Promise<AiSummary> => {
    const response = await api.post<ApiResponse<AiSummary>>(
      '/ai/summaries/generate',
      null,
      { params: { entityType, entityId, summaryType } }
    );
    return response.data.data;
  },

  getSummaries: async (entityType: string, entityId: string): Promise<AiSummary[]> => {
    const response = await api.get<ApiResponse<AiSummary[]>>(
      '/ai/summaries',
      { params: { entityType, entityId } }
    );
    return response.data.data;
  },

  approveSummary: async (summaryId: string): Promise<AiSummary> => {
    const response = await api.post<ApiResponse<AiSummary>>(`/ai/summaries/${summaryId}/approve`);
    return response.data.data;
  },

  // Duplicate Detection
  findSimilarItems: async (entityType: string, title: string, description?: string): Promise<SimilaritySuggestion[]> => {
    const response = await api.get<ApiResponse<SimilaritySuggestion[]>>(
      '/ai/duplicates/check',
      { params: { entityType, title, description } }
    );
    return response.data.data;
  },

  dismissSuggestion: async (suggestionId: string): Promise<void> => {
    await api.post(`/ai/duplicates/${suggestionId}/dismiss`);
  },

  // Recommendation Engine
  getRecommendationsForRelease: async (releaseId: string): Promise<ReleaseRecommendation[]> => {
    const response = await api.get<ApiResponse<ReleaseRecommendation[]>>(`/ai/recommendations/release/${releaseId}`);
    return response.data.data;
  },

  acceptRecommendation: async (recommendationId: string): Promise<void> => {
    await api.post(`/ai/recommendations/${recommendationId}/accept`);
  },

  dismissRecommendation: async (recommendationId: string): Promise<void> => {
    await api.post(`/ai/recommendations/${recommendationId}/dismiss`);
  },
};

export default aiService;
