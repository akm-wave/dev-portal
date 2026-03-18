import api from './api';
import { 
  UserNote, 
  UserNoteRequest, 
  UserReminder, 
  UserReminderRequest, 
  ReminderCounts,
  ReminderStatus,
  PagedResponse,
  WorkspaceProductivityDTO
} from '../types';

// Notes API
export const noteService = {
  getMyNotes: async (page = 0, size = 20, search?: string, archived = false): Promise<PagedResponse<UserNote>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size), archived: String(archived) });
    if (search) params.append('search', search);
    const response = await api.get(`/workspace/notes?${params}`);
    return response.data.data;
  },

  getPinnedNotes: async (): Promise<UserNote[]> => {
    const response = await api.get('/workspace/notes/pinned');
    return response.data.data;
  },

  getNoteById: async (id: string): Promise<UserNote> => {
    const response = await api.get(`/workspace/notes/${id}`);
    return response.data.data;
  },

  createNote: async (request: UserNoteRequest): Promise<UserNote> => {
    console.log(request);
    const response = await api.post('/workspace/notes', request);
    return response.data.data;
  },

  updateNote: async (id: string, request: UserNoteRequest): Promise<UserNote> => {
    const response = await api.put(`/workspace/notes/${id}`, request);
    return response.data.data;
  },

  deleteNote: async (id: string): Promise<void> => {
    await api.delete(`/workspace/notes/${id}`);
  },

  togglePin: async (id: string): Promise<UserNote> => {
    const response = await api.post(`/workspace/notes/${id}/toggle-pin`);
    return response.data.data;
  },

  toggleArchive: async (id: string): Promise<UserNote> => {
    const response = await api.post(`/workspace/notes/${id}/toggle-archive`);
    return response.data.data;
  },
};

// Reminders API
export const reminderService = {
  getMyReminders: async (page = 0, size = 20, status?: ReminderStatus): Promise<PagedResponse<UserReminder>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (status) params.append('status', status);
    const response = await api.get(`/workspace/reminders?${params}`);
    return response.data.data;
  },

  getTodayReminders: async (): Promise<UserReminder[]> => {
    const response = await api.get('/workspace/reminders/today');
    return response.data.data;
  },

  getOverdueReminders: async (): Promise<UserReminder[]> => {
    const response = await api.get('/workspace/reminders/overdue');
    return response.data.data;
  },

  getUpcomingReminders: async (): Promise<UserReminder[]> => {
    const response = await api.get('/workspace/reminders/upcoming');
    return response.data.data;
  },

  getReminderCounts: async (): Promise<ReminderCounts> => {
    const response = await api.get('/workspace/reminders/counts');
    return response.data.data;
  },

  getReminderById: async (id: string): Promise<UserReminder> => {
    const response = await api.get(`/workspace/reminders/${id}`);
    return response.data.data;
  },

  createReminder: async (request: UserReminderRequest): Promise<UserReminder> => {
    const response = await api.post('/workspace/reminders', request);
    return response.data.data;
  },

  updateReminder: async (id: string, request: UserReminderRequest): Promise<UserReminder> => {
    const response = await api.put(`/workspace/reminders/${id}`, request);
    return response.data.data;
  },

  deleteReminder: async (id: string): Promise<void> => {
    await api.delete(`/workspace/reminders/${id}`);
  },

  markAsCompleted: async (id: string): Promise<UserReminder> => {
    const response = await api.post(`/workspace/reminders/${id}/complete`);
    return response.data.data;
  },

  snoozeReminder: async (id: string, snoozeUntil: Date): Promise<UserReminder> => {
    const response = await api.post(`/workspace/reminders/${id}/snooze`, { snoozeUntil });
    return response.data.data;
  },
};

// Productivity API
export const productivityService = {
  getMyProductivityDashboard: async (dateRange = 'this_month'): Promise<WorkspaceProductivityDTO> => {
    console.log('[Productivity Service] Making request to:', `/workspace/productivity?dateRange=${dateRange}`);
    console.log('[Productivity Service] API client baseURL:', api.defaults.baseURL);
    const response = await api.get(`/api/workspace/productivity?dateRange=${dateRange}`);
    console.log('[Productivity Service] Response received:', response.status);
    return response.data.data;
  },
};
