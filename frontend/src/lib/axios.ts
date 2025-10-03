import axios from 'axios';
import { API_BASE_URL } from '@/config';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Add a request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    // Add any request headers here
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error('API Error:', JSON.stringify(error.response.data, null, 2));
      return Promise.reject(error.response.data);
    } else if (error.request) {
      // The request was made but no response was received
      console.error('Network Error:', error.request.toString());
      return Promise.reject({ message: 'Network error occurred' });
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Request Error:', error.message);
      return Promise.reject({ message: error.message });
    }
  }
);

// Export the instance as both default and named export
export const apiClient = axiosInstance;
export default axiosInstance;

// Export types
export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
}

export interface ApiError {
  message: string;
  status?: number;
  errors?: string[];
}

// Utility function to extract error message
export const getErrorMessage = (error: any): string => {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.message) {
    return error.message;
  }
  if (typeof error === 'string') {
    return error;
  }
  return 'An unexpected error occurred';
};