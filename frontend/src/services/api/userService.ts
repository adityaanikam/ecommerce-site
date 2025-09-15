import { apiClient, ApiResponse } from '@/lib/axios';
import { UserDto } from '@/types/api';

export interface UpdateUserProfileRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  avatar?: string;
}

export interface AddressDto {
  id: string;
  type: 'HOME' | 'WORK' | 'OTHER';
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  phoneNumber: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAddressRequest {
  type: 'HOME' | 'WORK' | 'OTHER';
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  phoneNumber: string;
  isDefault?: boolean;
}

export interface UpdateAddressRequest extends Partial<CreateAddressRequest> {
  id: string;
}

export interface WishlistItemDto {
  id: string;
  productId: string;
  product: {
    id: string;
    name: string;
    price: number;
    discountPrice?: number;
    images: string[];
    brand: string;
    inStock: boolean;
  };
  addedAt: string;
}

export interface NotificationSettings {
  emailNotifications: {
    orderUpdates: boolean;
    promotions: boolean;
    productUpdates: boolean;
    securityAlerts: boolean;
  };
  pushNotifications: {
    orderUpdates: boolean;
    promotions: boolean;
    productUpdates: boolean;
    securityAlerts: boolean;
  };
  smsNotifications: {
    orderUpdates: boolean;
    securityAlerts: boolean;
  };
}

export interface UserPreferences {
  currency: string;
  language: string;
  timezone: string;
  theme: 'light' | 'dark' | 'auto';
  emailFrequency: 'immediate' | 'daily' | 'weekly';
}

export class UserService {
  private static readonly BASE_PATH = '/users';

  // Get current user profile
  static async getCurrentUser(): Promise<ApiResponse<UserDto>> {
    const response = await apiClient.get(`${this.BASE_PATH}/me`);
    return response.data;
  }

  // Update user profile
  static async updateProfile(data: UpdateUserProfileRequest): Promise<ApiResponse<UserDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/profile`, data);
    return response.data;
  }

  // Upload user avatar
  static async uploadAvatar(file: File): Promise<ApiResponse<{ avatarUrl: string }>> {
    const formData = new FormData();
    formData.append('avatar', file);

    const response = await apiClient.post(`${this.BASE_PATH}/avatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  // Delete user avatar
  static async deleteAvatar(): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/avatar`);
    return response.data;
  }

  // Get user addresses
  static async getAddresses(): Promise<ApiResponse<AddressDto[]>> {
    const response = await apiClient.get(`${this.BASE_PATH}/addresses`);
    return response.data;
  }

  // Create new address
  static async createAddress(data: CreateAddressRequest): Promise<ApiResponse<AddressDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/addresses`, data);
    return response.data;
  }

  // Update address
  static async updateAddress(data: UpdateAddressRequest): Promise<ApiResponse<AddressDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/addresses/${data.id}`, data);
    return response.data;
  }

  // Delete address
  static async deleteAddress(addressId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/addresses/${addressId}`);
    return response.data;
  }

  // Set default address
  static async setDefaultAddress(addressId: string): Promise<ApiResponse<AddressDto[]>> {
    const response = await apiClient.put(`${this.BASE_PATH}/addresses/${addressId}/default`);
    return response.data;
  }

  // Get user wishlist
  static async getWishlist(params?: {
    page: number;
    limit: number;
  }): Promise<ApiResponse<{
    items: WishlistItemDto[];
    totalCount: number;
    totalPages: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/wishlist`, { params });
    return response.data;
  }

  // Add item to wishlist
  static async addToWishlist(productId: string): Promise<ApiResponse<WishlistItemDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/wishlist`, { productId });
    return response.data;
  }

  // Remove item from wishlist
  static async removeFromWishlist(productId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/wishlist/${productId}`);
    return response.data;
  }

  // Clear wishlist
  static async clearWishlist(): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/wishlist`);
    return response.data;
  }

  // Get notification settings
  static async getNotificationSettings(): Promise<ApiResponse<NotificationSettings>> {
    const response = await apiClient.get(`${this.BASE_PATH}/notification-settings`);
    return response.data;
  }

  // Update notification settings
  static async updateNotificationSettings(settings: NotificationSettings): Promise<ApiResponse<NotificationSettings>> {
    const response = await apiClient.put(`${this.BASE_PATH}/notification-settings`, settings);
    return response.data;
  }

  // Get user preferences
  static async getUserPreferences(): Promise<ApiResponse<UserPreferences>> {
    const response = await apiClient.get(`${this.BASE_PATH}/preferences`);
    return response.data;
  }

  // Update user preferences
  static async updateUserPreferences(preferences: UserPreferences): Promise<ApiResponse<UserPreferences>> {
    const response = await apiClient.put(`${this.BASE_PATH}/preferences`, preferences);
    return response.data;
  }

  // Get user activity log
  static async getActivityLog(params: {
    page: number;
    limit: number;
    type?: string;
  }): Promise<ApiResponse<{
    activities: Array<{
      id: string;
      type: string;
      description: string;
      timestamp: string;
      metadata?: any;
    }>;
    totalCount: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/activity-log`, { params });
    return response.data;
  }

  // Get user statistics
  static async getUserStatistics(): Promise<ApiResponse<{
    totalOrders: number;
    totalSpent: number;
    wishlistItems: number;
    addresses: number;
    memberSince: string;
    lastOrderDate?: string;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/statistics`);
    return response.data;
  }

  // Delete user account
  static async deleteAccount(password: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/account`, {
      data: { password }
    });
    return response.data;
  }

  // Export user data (GDPR compliance)
  static async exportUserData(): Promise<Blob> {
    const response = await apiClient.get(`${this.BASE_PATH}/export-data`, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Get user notifications
  static async getNotifications(params: {
    page: number;
    limit: number;
    unreadOnly?: boolean;
  }): Promise<ApiResponse<{
    notifications: Array<{
      id: string;
      type: string;
      title: string;
      message: string;
      isRead: boolean;
      createdAt: string;
      metadata?: any;
    }>;
    totalCount: number;
    unreadCount: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/notifications`, { params });
    return response.data;
  }

  // Mark notification as read
  static async markNotificationAsRead(notificationId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.put(`${this.BASE_PATH}/notifications/${notificationId}/read`);
    return response.data;
  }

  // Mark all notifications as read
  static async markAllNotificationsAsRead(): Promise<ApiResponse<void>> {
    const response = await apiClient.put(`${this.BASE_PATH}/notifications/read-all`);
    return response.data;
  }

  // Delete notification
  static async deleteNotification(notificationId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/notifications/${notificationId}`);
    return response.data;
  }

  // Get user reviews
  static async getUserReviews(params: {
    page: number;
    limit: number;
  }): Promise<ApiResponse<{
    reviews: Array<{
      id: string;
      productId: string;
      productName: string;
      productImage: string;
      rating: number;
      title: string;
      content: string;
      createdAt: string;
      isVerified: boolean;
    }>;
    totalCount: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/reviews`, { params });
    return response.data;
  }
}
