import axios, { AxiosInstance } from 'axios';

// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080';

/**
 * Get the full image URL by combining base URL with image path
 * @param path - The image path (can be relative or absolute)
 * @returns The full image URL
 */
export const getImageUrl = (path: string): string => {
  if (!path) {
    return `${IMAGE_BASE_URL}/placeholder.jpg`;
  }
  
  // If path is already a full URL, return as is
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // If path starts with '/', it's a relative path from the base URL
  if (path.startsWith('/')) {
    return `${IMAGE_BASE_URL}${path}`;
  }
  
  // Otherwise, treat as relative path
  return `${IMAGE_BASE_URL}/${path}`;
};

/**
 * Get fallback image URL for error handling
 * @param size - The size of the placeholder image (default: 800x800)
 * @returns The fallback image URL
 */
export const getFallbackImageUrl = (size: string = '800x800'): string => {
  return `https://placehold.co/${size}/6366f1/ffffff?text=Image+Not+Found`;
};

/**
 * Handle image load error by setting a fallback image
 * @param event - The error event
 * @param size - The size of the fallback image
 */
export const handleImageError = (event: React.SyntheticEvent<HTMLImageElement, Event>, size: string = '800x800') => {
  const target = event.currentTarget;
  target.src = getFallbackImageUrl(size);
};

// Create axios instance with proper configuration
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 10000, // 10 second timeout
});

// Add request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add any request headers here
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor
apiClient.interceptors.response.use(
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

export default apiClient;

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
