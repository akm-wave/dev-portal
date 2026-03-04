import api from './api';
import { ApiResponse } from '../types';
import { RelationshipData } from '../types/relationship';

export const relationshipService = {
  getRelationships: async (): Promise<RelationshipData> => {
    const response = await api.get<ApiResponse<RelationshipData>>('/relationships');
    return response.data.data;
  },
};
