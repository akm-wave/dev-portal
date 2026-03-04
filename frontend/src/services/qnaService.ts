import api from './api';

export interface UserSummary {
  id: string;
  username: string;
  fullName: string;
}

export interface AttachmentResponse {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  fileUrl: string;
  uploadedAt: string;
}

export interface HyperlinkResponse {
  id: string;
  url: string;
  title: string;
}

export interface QnaCommentResponse {
  id: string;
  answerId: string;
  content: string;
  createdBy: UserSummary;
  createdAt: string;
}

export interface QnaAnswerResponse {
  id: string;
  questionId: string;
  content: string;
  upvotes: number;
  isAccepted: boolean;
  createdBy: UserSummary;
  createdAt: string;
  updatedAt: string;
  attachments: AttachmentResponse[];
  hyperlinks: HyperlinkResponse[];
  comments: QnaCommentResponse[];
}

export interface QnaQuestionResponse {
  id: string;
  title: string;
  content: string;
  tags: string[];
  viewCount: number;
  upvotes: number;
  isResolved: boolean;
  answerCount: number;
  createdBy: UserSummary;
  createdAt: string;
  updatedAt: string;
  attachments: AttachmentResponse[];
  hyperlinks: HyperlinkResponse[];
  answers?: QnaAnswerResponse[];
}

export interface QnaQuestionRequest {
  title: string;
  content: string;
  tags?: string[];
  hyperlinks?: { url: string; title: string }[];
}

export interface QnaAnswerRequest {
  questionId: string;
  content: string;
  hyperlinks?: { url: string; title: string }[];
}

export interface QnaCommentRequest {
  answerId: string;
  content: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const qnaService = {
  getQuestions: async (params?: { filter?: string; tag?: string; page?: number; size?: number }): Promise<PageResponse<QnaQuestionResponse>> => {
    const response = await api.get<PageResponse<QnaQuestionResponse>>('/qna/questions', { params });
    return response.data;
  },

  getQuestion: async (id: string): Promise<QnaQuestionResponse> => {
    const response = await api.get<QnaQuestionResponse>(`/qna/questions/${id}`);
    return response.data;
  },

  createQuestion: async (request: QnaQuestionRequest): Promise<QnaQuestionResponse> => {
    const response = await api.post<QnaQuestionResponse>('/qna/questions', request);
    return response.data;
  },

  deleteQuestion: async (id: string): Promise<void> => {
    await api.delete(`/qna/questions/${id}`);
  },

  upvoteQuestion: async (id: string): Promise<void> => {
    await api.post(`/qna/questions/${id}/upvote`);
  },

  searchQuestions: async (q: string, page?: number, size?: number): Promise<PageResponse<QnaQuestionResponse>> => {
    const response = await api.get<PageResponse<QnaQuestionResponse>>('/qna/search', { params: { q, page, size } });
    return response.data;
  },

  createAnswer: async (request: QnaAnswerRequest): Promise<QnaAnswerResponse> => {
    const response = await api.post<QnaAnswerResponse>('/qna/answers', request);
    return response.data;
  },

  upvoteAnswer: async (id: string): Promise<void> => {
    await api.post(`/qna/answers/${id}/upvote`);
  },

  acceptAnswer: async (id: string): Promise<void> => {
    await api.post(`/qna/answers/${id}/accept`);
  },

  createComment: async (request: QnaCommentRequest): Promise<QnaCommentResponse> => {
    const response = await api.post<QnaCommentResponse>('/qna/comments', request);
    return response.data;
  },

  uploadAttachment: async (file: File, questionId?: string, answerId?: string): Promise<AttachmentResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    if (questionId) formData.append('questionId', questionId);
    if (answerId) formData.append('answerId', answerId);
    
    const response = await api.post<AttachmentResponse>('/qna/attachments', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
