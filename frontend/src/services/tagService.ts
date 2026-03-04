import api from './api';
import { ApiResponse, Tag, TagRequest } from '../types';

export const tagService = {
  getAll: async (): Promise<Tag[]> => {
    const response = await api.get<ApiResponse<Tag[]>>('/tags');
    return response.data.data;
  },

  getById: async (id: string): Promise<Tag> => {
    const response = await api.get<ApiResponse<Tag>>(`/tags/${id}`);
    return response.data.data;
  },

  search: async (query: string): Promise<Tag[]> => {
    const response = await api.get<ApiResponse<Tag[]>>('/tags/search', { params: { query } });
    return response.data.data;
  },

  create: async (request: TagRequest): Promise<Tag> => {
    const response = await api.post<ApiResponse<Tag>>('/tags', request);
    return response.data.data;
  },

  update: async (id: string, request: TagRequest): Promise<Tag> => {
    const response = await api.put<ApiResponse<Tag>>(`/tags/${id}`, request);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/tags/${id}`);
  },
};

export default tagService;
