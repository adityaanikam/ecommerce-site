import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

// Types
export interface ApiError {
  message: string;
  status: number;
  code?: string;
  fieldErrors?: Record<string, string>;
}

export interface ApiResponse<T = any> {
  data: T;
  message: string;
  status: number;
  timestamp: string;
  path: string;
}

// Create axios instance
const createAxiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor
  instance.interceptors.request.use(
    (config: AxiosRequestConfig) => {
      // Add auth token if available
      const token = localStorage.getItem('accessToken');
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      // Add request timestamp for debugging
      if (config.metadata) {
        config.metadata.startTime = new Date().getTime();
      }

      // Log request in development
      if (import.meta.env.DEV) {
        console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`, {
          data: config.data,
          params: config.params,
        });
      }

      return config;
    },
    (error: AxiosError) => {
      console.error('Request interceptor error:', error);
      return Promise.reject(error);
    }
  );

  // Response interceptor
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      // Log response in development
      if (import.meta.env.DEV) {
        const duration = response.config.metadata?.startTime 
          ? new Date().getTime() - response.config.metadata.startTime 
          : 0;
        console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, {
          status: response.status,
          duration: `${duration}ms`,
          data: response.data,
        });
      }

      return response;
    },
    async (error: AxiosError) => {
      const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

      // Log error in development
      if (import.meta.env.DEV) {
        console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
          status: error.response?.status,
          data: error.response?.data,
          message: error.message,
        });
      }

      // Handle 401 Unauthorized - Token refresh
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;

        try {
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            const response = await axios.post(
              `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/auth/refresh`,
              { refreshToken }
            );

            const { accessToken, refreshToken: newRefreshToken } = response.data.data;
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', newRefreshToken);

            // Retry original request with new token
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            }
            return instance(originalRequest);
          }
        } catch (refreshError) {
          // Refresh failed, redirect to login
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      }

      // Handle 403 Forbidden
      if (error.response?.status === 403) {
        // Show access denied message
        console.warn('Access denied - insufficient permissions');
      }

      // Handle 429 Too Many Requests
      if (error.response?.status === 429) {
        const retryAfter = error.response.headers['retry-after'];
        console.warn(`Rate limited. Retry after ${retryAfter} seconds`);
      }

      // Handle network errors
      if (!error.response) {
        console.error('Network error - please check your connection');
      }

      return Promise.reject(transformError(error));
    }
  );

  return instance;
};

// Transform axios error to our ApiError format
const transformError = (error: AxiosError): ApiError => {
  if (error.response) {
    const responseData = error.response.data as any;
    return {
      message: responseData?.message || error.message || 'An error occurred',
      status: error.response.status,
      code: responseData?.code,
      fieldErrors: responseData?.fieldErrors,
    };
  }

  if (error.request) {
    return {
      message: 'Network error - please check your connection',
      status: 0,
      code: 'NETWORK_ERROR',
    };
  }

  return {
    message: error.message || 'An unexpected error occurred',
    status: 0,
    code: 'UNKNOWN_ERROR',
  };
};

// Create and export the axios instance
export const apiClient = createAxiosInstance();

// Utility functions
export const isApiError = (error: any): error is ApiError => {
  return error && typeof error === 'object' && 'status' in error && 'message' in error;
};

export const getErrorMessage = (error: unknown): string => {
  if (isApiError(error)) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'An unexpected error occurred';
};

// Request/Response types for metadata
declare module 'axios' {
  interface AxiosRequestConfig {
    metadata?: {
      startTime: number;
    };
  }
}
