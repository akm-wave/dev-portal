import api from './api';
import { Domain, DomainRequest } from '../types';

export const domainService = {
  getAll: async (): Promise<Domain[]> => {
    const response = await api.get('/domains');
    return response.data;
  },

  getAllIncludingInactive: async (): Promise<Domain[]> => {
    const response = await api.get('/domains/all');
    return response.data;
  },

  getById: async (id: string): Promise<Domain> => {
    const response = await api.get(`/domains/${id}`);
    return response.data;
  },

  create: async (data: DomainRequest): Promise<Domain> => {
    const response = await api.post('/domains', data);
    return response.data;
  },

  update: async (id: string, data: DomainRequest): Promise<Domain> => {
    const response = await api.put(`/domains/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/domains/${id}`);
  },
};
