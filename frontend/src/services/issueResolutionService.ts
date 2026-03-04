import api from './api';
import { ApiResponse, PagedResponse, IssueResolution, IssueResolutionAttachment } from '../types';

export const issueResolutionService = {
  getResolutions: async (issueId: string, page = 0, size = 20): Promise<PagedResponse<IssueResolution>> => {
    const response = await api.get<ApiResponse<PagedResponse<IssueResolution>>>(
      `/issues/${issueId}/resolutions`,
      { params: { page, size } }
    );
    return response.data.data;
  },

  getAllResolutions: async (issueId: string): Promise<IssueResolution[]> => {
    const response = await api.get<ApiResponse<IssueResolution[]>>(`/issues/${issueId}/resolutions/all`);
    return response.data.data;
  },

  createResolution: async (
    issueId: string,
    comment: string,
    isResolutionComment: boolean,
    files?: File[]
  ): Promise<IssueResolution> => {
    const formData = new FormData();
    
    const data = { comment, isResolutionComment };
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    
    if (files && files.length > 0) {
      files.forEach((file) => {
        formData.append('files', file);
      });
    }

    const response = await api.post<ApiResponse<IssueResolution>>(
      `/issues/${issueId}/resolutions`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.data;
  },

  addAttachment: async (
    issueId: string,
    resolutionId: string,
    file: File
  ): Promise<IssueResolutionAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post<ApiResponse<IssueResolutionAttachment>>(
      `/issues/${issueId}/resolutions/${resolutionId}/attachments`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.data;
  },

  deleteAttachment: async (
    issueId: string,
    resolutionId: string,
    attachmentId: string
  ): Promise<void> => {
    await api.delete(`/issues/${issueId}/resolutions/${resolutionId}/attachments/${attachmentId}`);
  },

  downloadFile: (mongoFileId: string): string => {
    return `/api/files/${mongoFileId}`;
  },

  previewFile: (mongoFileId: string): string => {
    return `/api/files/${mongoFileId}/preview`;
  },
};
