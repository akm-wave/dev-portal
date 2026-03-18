import api from './api';
import { ApiResponse, PagedResponse, Issue, IssueRequest, IssueAttachment, IssueComment, IssueCommentRequest } from '../types';

export const issueService = {
  getAll: async (params?: { page?: number; size?: number; status?: string; search?: string }): Promise<PagedResponse<Issue>> => {
    const response = await api.get<ApiResponse<PagedResponse<Issue>>>('/issues', { params });
    return response.data.data;
  },

  getById: async (id: string): Promise<Issue> => {
    const response = await api.get<ApiResponse<Issue>>(`/issues/${id}`);
    return response.data.data;
  },

  getByAssignedUser: async (userId: string): Promise<Issue[]> => {
    const response = await api.get<ApiResponse<Issue[]>>(`/issues/assigned/${userId}`);
    return response.data.data;
  },

  create: async (data: IssueRequest): Promise<Issue> => {
    const response = await api.post<ApiResponse<Issue>>('/issues', data);
    return response.data.data;
  },

  update: async (id: string, data: IssueRequest): Promise<Issue> => {
    const response = await api.put<ApiResponse<Issue>>(`/issues/${id}`, data);
    return response.data.data;
  },

  assign: async (id: string, userId: string): Promise<Issue> => {
    const response = await api.patch<ApiResponse<Issue>>(`/issues/${id}/assign`, { userId });
    return response.data.data;
  },

  resolve: async (id: string, resultComment?: string, attachmentUrl?: string): Promise<Issue> => {
    const response = await api.patch<ApiResponse<Issue>>(`/issues/${id}/resolve`, { resultComment, attachmentUrl });
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/issues/${id}`);
  },

  // Resolution endpoints
  getAttachments: async (id: string): Promise<IssueAttachment[]> => {
    const response = await api.get<ApiResponse<IssueAttachment[]>>(`/issues/${id}/attachments`);
    return response.data.data;
  },

  uploadAttachment: async (id: string, file: File): Promise<IssueAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<ApiResponse<IssueAttachment>>(`/issues/${id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },

  deleteAttachment: async (issueId: string, attachmentId: string): Promise<void> => {
    await api.delete(`/issues/${issueId}/attachments/${attachmentId}`);
  },

  getComments: async (id: string): Promise<IssueComment[]> => {
    const response = await api.get<ApiResponse<IssueComment[]>>(`/issues/${id}/comments`);
    return response.data.data;
  },

  addComment: async (id: string, data: IssueCommentRequest): Promise<IssueComment> => {
    const response = await api.post<ApiResponse<IssueComment>>(`/issues/${id}/comments`, data);
    return response.data.data;
  },

  deleteComment: async (issueId: string, commentId: string): Promise<void> => {
    await api.delete(`/issues/${issueId}/comments/${commentId}`);
  },

  isOwner: async (id: string): Promise<boolean> => {
    const response = await api.get<ApiResponse<boolean>>(`/issues/${id}/is-owner`);
    return response.data.data;
  },
};
