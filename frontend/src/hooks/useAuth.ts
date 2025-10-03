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
      
      // Update user data in cache
      queryClient.setQueryData(queryKeys.auth.currentUser, user);
      
      // Invalidate and refetch user data
      invalidateQueries.auth();
      
      showSuccess(`Welcome back, ${user.firstName}!`);
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
      
      // Update user data in cache
      queryClient.setQueryData(queryKeys.auth.currentUser, user);
      
      // Invalidate and refetch user data
      invalidateQueries.auth();
      
      showSuccess(`Welcome to our store, ${user.firstName}!`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for logout
export const useLogout = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.logout,
    onSuccess: () => {
      // Clear tokens
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      
      // Clear all cached data
      queryClient.clear();
      
      showSuccess('You have been logged out successfully');
    },
    onError: (error: ApiError) => {
      // Even if logout fails on server, clear local data
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      queryClient.clear();
      
      showError(getErrorMessage(error));
    },
  });
};

// Hook for updating profile
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.updateProfile,
    onSuccess: (response) => {
      // Update user data in cache
      queryClient.setQueryData(queryKeys.auth.currentUser, response.data);
      invalidateQueries.userProfile();
      
      showSuccess('Profile updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for changing password
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

// Hook for forgot password
export const useForgotPassword = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.forgotPassword,
    onSuccess: () => {
      showSuccess('Password reset email sent. Please check your inbox.');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for reset password
export const useResetPassword = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.resetPassword,
    onSuccess: () => {
      showSuccess('Password reset successfully. You can now log in with your new password.');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for Google OAuth
export const useGoogleAuth = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.googleAuth,
    onSuccess: (response) => {
      const { accessToken, refreshToken, user } = response.data;
      
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Update user data in cache
      queryClient.setQueryData(queryKeys.auth.currentUser, user);
      
      // Invalidate and refetch user data
      invalidateQueries.auth();
      
      showSuccess(`Welcome, ${user.firstName}!`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for email verification
export const useVerifyEmail = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.verifyEmail,
    onSuccess: () => {
      showSuccess('Email verified successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for resending verification email
export const useResendVerificationEmail = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.resendVerificationEmail,
    onSuccess: () => {
      showSuccess('Verification email sent. Please check your inbox.');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for deleting account
export const useDeleteAccount = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: AuthService.deleteAccount,
    onSuccess: () => {
      // Clear tokens and data
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
