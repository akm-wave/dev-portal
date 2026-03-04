import api from './api';
import { 
  Release, 
  ReleaseRequest, 
  ReleaseMicroservice, 
  ReleaseMicroserviceRequest,
  ReleaseLink,
  ReleaseLinkRequest,
  ReleaseStatus,
  PagedResponse 
} from '../types';

const releaseService = {
  getAll: async (
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    sortDir = 'desc',
    status?: ReleaseStatus,
    search?: string
  ): Promise<PagedResponse<Release>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });
    if (status) params.append('status', status);
    if (search) params.append('search', search);
    
    const response = await api.get(`/releases?${params.toString()}`);
    return response.data;
  },

  getById: async (id: string): Promise<Release> => {
    const response = await api.get(`/releases/${id}`);
    return response.data;
  },

  create: async (request: ReleaseRequest): Promise<Release> => {
    const response = await api.post('/releases', request);
    return response.data;
  },

  update: async (id: string, request: ReleaseRequest): Promise<Release> => {
    const response = await api.put(`/releases/${id}`, request);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/releases/${id}`);
  },

  getMicroservices: async (releaseId: string): Promise<ReleaseMicroservice[]> => {
    const response = await api.get(`/releases/${releaseId}/microservices`);
    return response.data;
  },

  addMicroservice: async (releaseId: string, request: ReleaseMicroserviceRequest): Promise<ReleaseMicroservice> => {
    const response = await api.post(`/releases/${releaseId}/microservices`, request);
    return response.data;
  },

  updateMicroservice: async (
    releaseId: string, 
    microserviceId: string, 
    request: ReleaseMicroserviceRequest
  ): Promise<ReleaseMicroservice> => {
    const response = await api.put(`/releases/${releaseId}/microservices/${microserviceId}`, request);
    return response.data;
  },

  removeMicroservice: async (releaseId: string, microserviceId: string): Promise<void> => {
    await api.delete(`/releases/${releaseId}/microservices/${microserviceId}`);
  },

  getLinks: async (releaseId: string): Promise<ReleaseLink[]> => {
    const response = await api.get(`/releases/${releaseId}/links`);
    return response.data;
  },

  addLink: async (releaseId: string, request: ReleaseLinkRequest): Promise<ReleaseLink> => {
    const response = await api.post(`/releases/${releaseId}/links`, request);
    return response.data;
  },

  removeLink: async (releaseId: string, linkId: string): Promise<void> => {
    await api.delete(`/releases/${releaseId}/links/${linkId}`);
  },
};

export default releaseService;
