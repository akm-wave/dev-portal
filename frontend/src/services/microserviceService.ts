import api from './api';
import { ApiResponse, PagedResponse, Microservice, MicroserviceRequest, MicroserviceStatus } from '../types';

interface GetMicroservicesParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  status?: MicroserviceStatus;
  search?: string;
}

export const microserviceService = {
  getAll: async (params: GetMicroservicesParams = {}): Promise<PagedResponse<Microservice>> => {
    const response = await api.get<ApiResponse<PagedResponse<Microservice>>>('/microservices', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Microservice> => {
    const response = await api.get<ApiResponse<Microservice>>(`/microservices/${id}`);
    return response.data.data;
  },

  create: async (data: MicroserviceRequest): Promise<Microservice> => {
    const response = await api.post<ApiResponse<Microservice>>('/microservices', data);
    return response.data.data;
  },

  update: async (id: string, data: MicroserviceRequest): Promise<Microservice> => {
    const response = await api.put<ApiResponse<Microservice>>(`/microservices/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/microservices/${id}`);
  },
};
