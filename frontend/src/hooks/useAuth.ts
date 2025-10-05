import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AuthService, LoginRequest, RegisterRequest, ChangePasswordRequest } from '@/services/api/index';
import { UserDto } from '@/types/api';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';
import { useNotifications } from '@/contexts/NotificationContext';

// Hook for getting current user
export const useCurrentUser = () => {
  return useQuery({
    queryKey: queryKeys.auth.currentUser,
    queryFn: async () => {
      const response = await AuthService.getCurrentUser();
      return response.data;
    },
    enabled: !!localStorage.getItem('accessToken'),
    retry: false,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for login
export const useLogin = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.login,
    onSuccess: (response) => {
      const { accessToken, refreshToken, user } = response.data;
      
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Update cache
      queryClient.setQueryData(queryKeys.auth.currentUser, user);
      
      showSuccess('Login successful');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for register
export const useRegister = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.register,
    onSuccess: (response) => {
      const { accessToken, refreshToken, user } = response.data;
      
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Update cache
      queryClient.setQueryData(queryKeys.auth.currentUser, user);
      
      showSuccess('Registration successful');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for logout
export const useLogout = () => {
  const queryClient = useQueryClient();
  const { showSuccess } = useNotifications();

  return useMutation({
    mutationFn: AuthService.logout,
    onSuccess: () => {
      // Clear tokens
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      
      // Clear cache
      queryClient.clear();
      
      showSuccess('Logged out successfully');
    },
    onError: (error: ApiError) => {
      // Even if logout fails on server, clear local state
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      queryClient.clear();
    },
  });
};

// Hook for refresh token
export const useRefreshToken = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: AuthService.refreshToken,
    onSuccess: (response) => {
      const { accessToken, refreshToken } = response.data;
      
      // Update tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
    },
    onError: () => {
      // If refresh fails, logout user
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      queryClient.clear();
    },
  });
};

// Hook for change password
export const useChangePassword = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.changePassword,
    onSuccess: () => {
      showSuccess('Password changed successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for update profile
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.updateProfile,
    onSuccess: (response) => {
      // Update cache with new user data
      queryClient.setQueryData(queryKeys.auth.currentUser, response.data);
      showSuccess('Profile updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for delete account
export const useDeleteAccount = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.deleteAccount,
    onSuccess: () => {
      // Clear tokens and cache
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      queryClient.clear();
      
      showSuccess('Account deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Main useAuth hook that combines all auth functionality
export const useAuth = () => {
  const currentUser = useCurrentUser();
  const login = useLogin();
  const register = useRegister();
  const logout = useLogout();
  const refreshToken = useRefreshToken();
  const changePassword = useChangePassword();
  const updateProfile = useUpdateProfile();
  const deleteAccount = useDeleteAccount();

  return {
    user: currentUser.data,
    isLoading: currentUser.isLoading,
    isAuthenticated: !!currentUser.data,
    login: login.mutate,
    register: register.mutate,
    logout: logout.mutate,
    refreshToken: refreshToken.mutate,
    changePassword: changePassword.mutate,
    updateProfile: updateProfile.mutate,
    deleteAccount: deleteAccount.mutate,
  };
};