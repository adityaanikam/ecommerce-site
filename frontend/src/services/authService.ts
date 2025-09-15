import { apiService } from './api';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  RefreshTokenRequest,
  PasswordChangeRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  User,
  ApiResponse,
} from '@/types';

export const authService = {
  // Login user
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiService.post<AuthResponse>('/auth/login', credentials);
    return response.data!;
  },

  // Register user
  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiService.post<AuthResponse>('/auth/register', userData);
    return response.data!;
  },

  // Refresh token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiService.post<AuthResponse>('/auth/refresh', { refreshToken });
    return response.data!;
  },

  // Logout user
  logout: async (): Promise<void> => {
    await apiService.post('/auth/logout');
  },

  // Logout from all devices
  logoutAllDevices: async (): Promise<void> => {
    await apiService.post('/auth/logout-all');
  },

  // Change password
  changePassword: async (passwordData: PasswordChangeRequest): Promise<void> => {
    await apiService.post('/auth/change-password', passwordData);
  },

  // Forgot password
  forgotPassword: async (email: string): Promise<void> => {
    await apiService.post('/auth/forgot-password', { email });
  },

  // Reset password
  resetPassword: async (resetData: ResetPasswordRequest): Promise<void> => {
    await apiService.post('/auth/reset-password', resetData);
  },

  // Get current user
  getCurrentUser: async (): Promise<User> => {
    const response = await apiService.get<User>('/auth/me');
    return response.data!;
  },

  // Verify email
  verifyEmail: async (token: string): Promise<void> => {
    await apiService.post('/auth/verify-email', { token });
  },

  // OAuth login (Google)
  googleLogin: async (): Promise<void> => {
    window.location.href = `${import.meta.env.VITE_API_URL}/oauth2/authorization/google`;
  },
};
