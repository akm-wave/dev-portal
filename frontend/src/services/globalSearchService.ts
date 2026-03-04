import api from './api';
import { ApiResponse, GlobalSearchResult } from '../types';

export const globalSearchService = {
  search: async (query: string, limit: number = 10): Promise<GlobalSearchResult> => {
    const response = await api.get<ApiResponse<GlobalSearchResult>>('/global-search', {
      params: { q: query, limit },
    });
    return response.data.data;
  },
};
