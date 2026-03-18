import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const url = (config.baseURL || '') + (config.url || '');
    const params = config.params ? JSON.stringify(config.params) : '';
    const method = config.method?.toUpperCase();
    const data = ((): string => {
      if (!method || method === 'GET') return '';
      if (config.data == null) return '';
      try {
        const raw = typeof config.data === 'string' ? config.data : JSON.stringify(config.data);
        return raw.length > 800 ? raw.slice(0, 800) + '...(truncated)' : raw;
      } catch {
        return '[unserializable request body]';
      }
    })();
    console.log('API Request:', method, url, params, data);
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
