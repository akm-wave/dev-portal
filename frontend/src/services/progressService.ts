import api from './api';

export interface UserProgress {
  userId: string;
  username: string;
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  blockedTasks: number;
  pendingTasks: number;
  totalWeight: number;
  completedWeight: number;
  progress: number;
  progressLevel: string;
  emoji: string;
}

export const progressService = {
  getMyProgress: async (): Promise<UserProgress> => {
    const response = await api.get<UserProgress>('/progress/me');
    return response.data;
  },
};
