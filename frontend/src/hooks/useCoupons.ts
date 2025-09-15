import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { useNotifications } from '@/contexts/NotificationContext';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';

interface Coupon {
  id: string;
  code: string;
  name: string;
  description: string;
  type: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING' | 'BUY_X_GET_Y';
  value: number; // Percentage or fixed amount
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit?: number;
  usedCount: number;
  isActive: boolean;
  validFrom: string;
  validUntil: string;
  applicableProducts?: string[]; // Product IDs
  applicableCategories?: string[]; // Category IDs
  excludedProducts?: string[]; // Product IDs
  excludedCategories?: string[]; // Category IDs
  userRestrictions?: {
    newUsersOnly?: boolean;
    existingUsersOnly?: boolean;
    minOrderCount?: number;
    maxOrderCount?: number;
  };
  createdAt: string;
  updatedAt: string;
}

interface AppliedCoupon {
  coupon: Coupon;
  discountAmount: number;
  originalAmount: number;
  finalAmount: number;
  appliedAt: string;
}

interface CouponValidation {
  isValid: boolean;
  coupon?: Coupon;
  discountAmount?: number;
  error?: string;
  warnings?: string[];
}

interface CreateCouponRequest {
  code: string;
  name: string;
  description: string;
  type: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING' | 'BUY_X_GET_Y';
  value: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit?: number;
  validFrom: string;
  validUntil: string;
  applicableProducts?: string[];
  applicableCategories?: string[];
  excludedProducts?: string[];
  excludedCategories?: string[];
  userRestrictions?: {
    newUsersOnly?: boolean;
    existingUsersOnly?: boolean;
    minOrderCount?: number;
    maxOrderCount?: number;
  };
}

// Hook for available coupons
export const useAvailableCoupons = (cartTotal?: number) => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ['coupons', 'available', cartTotal],
    queryFn: async (): Promise<Coupon[]> => {
      const params = new URLSearchParams();
      if (cartTotal) params.append('cartTotal', cartTotal.toString());

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/coupons/available?${params}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch available coupons');
      }
      
      return response.json();
    },
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for user's applied coupons
export const useAppliedCoupons = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ['coupons', 'applied'],
    queryFn: async (): Promise<AppliedCoupon[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/applied-coupons`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch applied coupons');
      }
      
      return response.json();
    },
    enabled: isAuthenticated,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for coupon validation
export const useCouponValidation = () => {
  const { showSuccess, showError } = useNotifications();

  const validateCouponMutation = useMutation({
    mutationFn: async ({ code, cartItems, cartTotal }: {
      code: string;
      cartItems: Array<{ productId: string; quantity: number; price: number }>;
      cartTotal: number;
    }): Promise<CouponValidation> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/coupons/validate`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code, cartItems, cartTotal }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to validate coupon');
      }
      
      return response.json();
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    validateCoupon: validateCouponMutation.mutateAsync,
    isValidating: validateCouponMutation.isPending,
  };
};

// Hook for coupon management
export const useCouponManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Apply coupon
  const applyCouponMutation = useMutation({
    mutationFn: async (code: string): Promise<AppliedCoupon> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/coupons/apply`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to apply coupon');
      }
      
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coupons'] });
      invalidateQueries.cart();
      showSuccess('Coupon applied successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Remove coupon
  const removeCouponMutation = useMutation({
    mutationFn: async (couponId: string): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/coupons/${couponId}/remove`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to remove coupon');
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coupons'] });
      invalidateQueries.cart();
      showSuccess('Coupon removed successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Clear all coupons
  const clearCouponsMutation = useMutation({
    mutationFn: async (): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/coupons/clear`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to clear coupons');
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['coupons'] });
      invalidateQueries.cart();
      showSuccess('All coupons removed successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    applyCoupon: applyCouponMutation.mutateAsync,
    removeCoupon: removeCouponMutation.mutateAsync,
    clearCoupons: clearCouponsMutation.mutateAsync,
    isApplying: applyCouponMutation.isPending,
    isRemoving: removeCouponMutation.isPending,
    isClearing: clearCouponsMutation.isPending,
  };
};

// Hook for admin coupon management
export const useAdminCouponManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Get all coupons (admin)
  const getAllCoupons = useQuery({
    queryKey: ['admin', 'coupons'],
    queryFn: async (): Promise<Coupon[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/coupons`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch coupons');
      }
      
      return response.json();
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  // Create coupon
  const createCouponMutation = useMutation({
    mutationFn: async (data: CreateCouponRequest): Promise<Coupon> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/coupons`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to create coupon');
      }
      
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] });
      showSuccess('Coupon created successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Update coupon
  const updateCouponMutation = useMutation({
    mutationFn: async ({ couponId, data }: { couponId: string; data: Partial<CreateCouponRequest> }): Promise<Coupon> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/coupons/${couponId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to update coupon');
      }
      
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] });
      showSuccess('Coupon updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Delete coupon
  const deleteCouponMutation = useMutation({
    mutationFn: async (couponId: string): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/coupons/${couponId}`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to delete coupon');
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] });
      showSuccess('Coupon deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    coupons: getAllCoupons.data || [],
    isLoading: getAllCoupons.isLoading,
    createCoupon: createCouponMutation.mutateAsync,
    updateCoupon: updateCouponMutation.mutateAsync,
    deleteCoupon: deleteCouponMutation.mutateAsync,
    isCreating: createCouponMutation.isPending,
    isUpdating: updateCouponMutation.isPending,
    isDeleting: deleteCouponMutation.isPending,
  };
};

// Hook for coupon analytics
export const useCouponAnalytics = () => {
  return useQuery({
    queryKey: ['admin', 'coupons', 'analytics'],
    queryFn: async () => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/coupons/analytics`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch coupon analytics');
      }
      
      return response.json();
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Utility functions for coupons
export const couponUtils = {
  // Get coupon type display name
  getTypeDisplayName: (type: string): string => {
    const typeMap: Record<string, string> = {
      PERCENTAGE: 'Percentage Discount',
      FIXED_AMOUNT: 'Fixed Amount Discount',
      FREE_SHIPPING: 'Free Shipping',
      BUY_X_GET_Y: 'Buy X Get Y',
    };
    return typeMap[type] || type;
  },

  // Calculate discount amount
  calculateDiscount: (coupon: Coupon, cartTotal: number): number => {
    switch (coupon.type) {
      case 'PERCENTAGE':
        const percentageDiscount = (cartTotal * coupon.value) / 100;
        return coupon.maxDiscountAmount 
          ? Math.min(percentageDiscount, coupon.maxDiscountAmount)
          : percentageDiscount;
      case 'FIXED_AMOUNT':
        return Math.min(coupon.value, cartTotal);
      case 'FREE_SHIPPING':
        return 0; // Free shipping is handled separately
      case 'BUY_X_GET_Y':
        return 0; // Complex logic needed
      default:
        return 0;
    }
  },

  // Check if coupon is valid for cart
  isValidForCart: (coupon: Coupon, cartTotal: number, cartItems: any[]): boolean => {
    // Check minimum order amount
    if (coupon.minOrderAmount && cartTotal < coupon.minOrderAmount) {
      return false;
    }

    // Check usage limit
    if (coupon.usageLimit && coupon.usedCount >= coupon.usageLimit) {
      return false;
    }

    // Check validity period
    const now = new Date();
    const validFrom = new Date(coupon.validFrom);
    const validUntil = new Date(coupon.validUntil);
    
    if (now < validFrom || now > validUntil) {
      return false;
    }

    // Check if coupon is active
    if (!coupon.isActive) {
      return false;
    }

    return true;
  },

  // Get coupon status
  getCouponStatus: (coupon: Coupon): 'ACTIVE' | 'EXPIRED' | 'USED_UP' | 'INACTIVE' => {
    const now = new Date();
    const validUntil = new Date(coupon.validUntil);

    if (!coupon.isActive) return 'INACTIVE';
    if (now > validUntil) return 'EXPIRED';
    if (coupon.usageLimit && coupon.usedCount >= coupon.usageLimit) return 'USED_UP';
    return 'ACTIVE';
  },

  // Get coupon status color
  getCouponStatusColor: (status: string): string => {
    switch (status) {
      case 'ACTIVE':
        return 'text-success-600 dark:text-success-400';
      case 'EXPIRED':
        return 'text-error-600 dark:text-error-400';
      case 'USED_UP':
        return 'text-warning-600 dark:text-warning-400';
      case 'INACTIVE':
        return 'text-secondary-600 dark:text-secondary-400';
      default:
        return 'text-secondary-600 dark:text-secondary-400';
    }
  },

  // Format coupon value
  formatCouponValue: (coupon: Coupon): string => {
    switch (coupon.type) {
      case 'PERCENTAGE':
        return `${coupon.value}% off`;
      case 'FIXED_AMOUNT':
        return `$${coupon.value} off`;
      case 'FREE_SHIPPING':
        return 'Free Shipping';
      case 'BUY_X_GET_Y':
        return 'Buy X Get Y';
      default:
        return coupon.value.toString();
    }
  },

  // Get coupon usage percentage
  getUsagePercentage: (coupon: Coupon): number => {
    if (!coupon.usageLimit) return 0;
    return Math.round((coupon.usedCount / coupon.usageLimit) * 100);
  },
};

export default useCoupons;
