import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { UserService } from '@/services/api';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  timestamp: number;
}

export interface ToastNotification extends Notification {
  id: string;
  isVisible: boolean;
  isRemoving: boolean;
}

interface NotificationContextType {
  // Toast notifications
  notifications: ToastNotification[];
  addNotification: (notification: Omit<Notification, 'id' | 'timestamp'>) => void;
  removeNotification: (id: string) => void;
  clearAllNotifications: () => void;
  
  // Success helpers
  showSuccess: (message: string, title?: string) => void;
  showError: (message: string, title?: string) => void;
  showWarning: (message: string, title?: string) => void;
  showInfo: (message: string, title?: string) => void;
  
  // API notifications
  apiNotifications: any[];
  unreadCount: number;
  markAsRead: (notificationId: string) => void;
  markAllAsRead: () => void;
  deleteNotification: (notificationId: string) => void;
  isLoading: boolean;
  error: string | null;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<ToastNotification[]>([]);
  const queryClient = useQueryClient();

  // Get API notifications
  const {
    data: apiNotificationsData,
    isLoading,
    error: apiError,
  } = useQuery({
    queryKey: queryKeys.users.notifications,
    queryFn: async () => {
      const response = await UserService.getNotifications({
        page: 1,
        limit: 50,
        unreadOnly: false,
      });
      return response.data;
    },
    enabled: !!localStorage.getItem('accessToken'),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  const apiNotifications = apiNotificationsData?.notifications || [];
  const unreadCount = apiNotificationsData?.unreadCount || 0;

  // Mark notification as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: UserService.markNotificationAsRead,
    onSuccess: () => {
      invalidateQueries.users();
    },
  });

  // Mark all notifications as read mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: UserService.markAllNotificationsAsRead,
    onSuccess: () => {
      invalidateQueries.users();
    },
  });

  // Delete notification mutation
  const deleteNotificationMutation = useMutation({
    mutationFn: UserService.deleteNotification,
    onSuccess: () => {
      invalidateQueries.users();
    },
  });

  // Add toast notification
  const addNotification = useCallback((notification: Omit<Notification, 'id' | 'timestamp'>) => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const newNotification: ToastNotification = {
      ...notification,
      id,
      timestamp: Date.now(),
      isVisible: true,
      isRemoving: false,
    };

    setNotifications(prev => [...prev, newNotification]);

    // Auto-remove after duration
    const duration = notification.duration || 5000;
    setTimeout(() => {
      removeNotification(id);
    }, duration);
  }, []);

  // Remove toast notification
  const removeNotification = useCallback((id: string) => {
    setNotifications(prev => 
      prev.map(notification => 
        notification.id === id 
          ? { ...notification, isRemoving: true }
          : notification
      )
    );

    // Remove from state after animation
    setTimeout(() => {
      setNotifications(prev => prev.filter(notification => notification.id !== id));
    }, 300);
  }, []);

  // Clear all toast notifications
  const clearAllNotifications = useCallback(() => {
    setNotifications(prev => 
      prev.map(notification => ({ ...notification, isRemoving: true }))
    );

    setTimeout(() => {
      setNotifications([]);
    }, 300);
  }, []);

  // Helper functions for different notification types
  const showSuccess = useCallback((message: string, title: string = 'Success') => {
    addNotification({
      type: 'success',
      title,
      message,
      duration: 4000,
    });
  }, [addNotification]);

  const showError = useCallback((message: string, title: string = 'Error') => {
    addNotification({
      type: 'error',
      title,
      message,
      duration: 6000, // Longer duration for errors
    });
  }, [addNotification]);

  const showWarning = useCallback((message: string, title: string = 'Warning') => {
    addNotification({
      type: 'warning',
      title,
      message,
      duration: 5000,
    });
  }, [addNotification]);

  const showInfo = useCallback((message: string, title: string = 'Info') => {
    addNotification({
      type: 'info',
      title,
      message,
      duration: 4000,
    });
  }, [addNotification]);

  // API notification actions
  const markAsRead = useCallback(async (notificationId: string) => {
    await markAsReadMutation.mutateAsync(notificationId);
  }, [markAsReadMutation]);

  const markAllAsRead = useCallback(async () => {
    await markAllAsReadMutation.mutateAsync();
  }, [markAllAsReadMutation]);

  const deleteNotification = useCallback(async (notificationId: string) => {
    await deleteNotificationMutation.mutateAsync(notificationId);
  }, [deleteNotificationMutation]);

  // Global error handler
  useEffect(() => {
    const handleGlobalError = (event: ErrorEvent) => {
      showError(event.message || 'An unexpected error occurred');
    };

    const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
      const error = event.reason;
      if (error && typeof error === 'object' && 'message' in error) {
        showError(error.message || 'An unexpected error occurred');
      } else {
        showError('An unexpected error occurred');
      }
    };

    window.addEventListener('error', handleGlobalError);
    window.addEventListener('unhandledrejection', handleUnhandledRejection);

    return () => {
      window.removeEventListener('error', handleGlobalError);
      window.removeEventListener('unhandledrejection', handleUnhandledRejection);
    };
  }, [showError]);

  const value: NotificationContextType = {
    notifications,
    addNotification,
    removeNotification,
    clearAllNotifications,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    apiNotifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    isLoading,
    error: apiError ? getErrorMessage(apiError) : null,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
