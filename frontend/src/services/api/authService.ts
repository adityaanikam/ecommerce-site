import { apiClient, ApiResponse } from '@/lib/axios';
import { AuthDto, UserDto } from '@/types/api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserDto;
  expiresIn: number;
}

export interface GoogleAuthRequest {
  token: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export class AuthService {
  private static readonly BASE_PATH = '/auth';

  // Login with email and password
  static async login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/login`, credentials);
    return response.data;
  }

  // Register new user
  static async register(userData: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/register`, userData);
    return response.data;
  }

  // Refresh access token
  static async refreshToken(data: RefreshTokenRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/refresh`, data);
    return response.data;
  }

  // Logout
  static async logout(): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/logout`);
    return response.data;
  }

  // Google OAuth login
  static async googleAuth(data: GoogleAuthRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/google`, data);
    return response.data;
  }

  // Forgot password
  static async forgotPassword(data: ForgotPasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/forgot-password`, data);
    return response.data;
  }

  // Reset password
  static async resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/reset-password`, data);
    return response.data;
  }

  // Change password
  static async changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/change-password`, data);
    return response.data;
  }

  // Verify email
  static async verifyEmail(token: string): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/verify-email`, { token });
    return response.data;
  }

  // Resend verification email
  static async resendVerificationEmail(): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${AuthService.BASE_PATH}/resend-verification`);
    return response.data;
  }

  // Get current user profile
  static async getCurrentUser(): Promise<ApiResponse<UserDto>> {
    const response = await apiClient.get(`${AuthService.BASE_PATH}/me`);
    return response.data;
  }

  // Update user profile
  static async updateProfile(data: Partial<UserDto>): Promise<ApiResponse<UserDto>> {
    const response = await apiClient.put(`${AuthService.BASE_PATH}/profile`, data);
    return response.data;
  }

  // Delete account
  static async deleteAccount(): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${AuthService.BASE_PATH}/account`);
    return response.data;
  }
}
