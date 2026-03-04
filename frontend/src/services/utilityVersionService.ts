import api from './api';
import { ApiResponse, UtilityVersion } from '../types';

export const utilityVersionService = {
  getHistory: async (utilityId: string): Promise<UtilityVersion[]> => {
    const response = await api.get<ApiResponse<UtilityVersion[]>>(`/utilities/${utilityId}/versions`);
    return response.data.data;
  },

  getVersion: async (utilityId: string, versionNumber: number): Promise<UtilityVersion> => {
    const response = await api.get<ApiResponse<UtilityVersion>>(`/utilities/${utilityId}/versions/${versionNumber}`);
    return response.data.data;
  },

  getCurrent: async (utilityId: string): Promise<UtilityVersion | null> => {
    const response = await api.get<ApiResponse<UtilityVersion>>(`/utilities/${utilityId}/versions/current`);
    return response.data.data;
  },

  createVersion: async (utilityId: string, changeSummary?: string): Promise<UtilityVersion> => {
    const response = await api.post<ApiResponse<UtilityVersion>>(
      `/utilities/${utilityId}/versions`,
      null,
      { params: { changeSummary } }
    );
    return response.data.data;
  },

  revertToVersion: async (utilityId: string, versionNumber: number): Promise<UtilityVersion> => {
    const response = await api.post<ApiResponse<UtilityVersion>>(
      `/utilities/${utilityId}/versions/revert/${versionNumber}`
    );
    return response.data.data;
  },

  compareVersions: async (utilityId: string, version1: number, version2: number): Promise<string> => {
    const response = await api.get<ApiResponse<string>>(
      `/utilities/${utilityId}/versions/compare`,
      { params: { version1, version2 } }
    );
    return response.data.data;
  },
};

export default utilityVersionService;
