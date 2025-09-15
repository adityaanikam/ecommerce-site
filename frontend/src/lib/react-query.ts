import { QueryClient, DefaultOptions } from '@tanstack/react-query';
import { ApiError } from './axios';

// Optimized query options for performance
const queryConfig: DefaultOptions = {
  queries: {
    // Time in milliseconds that data remains fresh - increased for better performance
    staleTime: 10 * 60 * 1000, // 10 minutes
    // Time in milliseconds that unused/inactive cache data remains in memory - increased for better caching
    gcTime: 30 * 60 * 1000, // 30 minutes (formerly cacheTime)
    // Retry failed requests with smart retry logic
    retry: (failureCount, error) => {
      // Don't retry on 4xx errors (client errors)
      if (error && typeof error === 'object' && 'status' in error) {
        const apiError = error as ApiError;
        if (apiError.status >= 400 && apiError.status < 500) {
          return false;
        }
      }
      // Don't retry more than 2 times for better performance
      return failureCount < 2;
    },
    // Retry delay with exponential backoff - reduced for faster recovery
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000),
    // Refetch on window focus - disabled for better UX
    refetchOnWindowFocus: false,
    // Refetch on reconnect - enabled for data consistency
    refetchOnReconnect: true,
    // Refetch on mount - disabled for better performance
    refetchOnMount: false,
    // Network mode for offline support
    networkMode: 'online',
  },
  mutations: {
    // Retry failed mutations with smart retry logic
    retry: (failureCount, error) => {
      // Don't retry on 4xx errors
      if (error && typeof error === 'object' && 'status' in error) {
        const apiError = error as ApiError;
        if (apiError.status >= 400 && apiError.status < 500) {
          return false;
        }
      }
      // Retry only once for mutations
      return failureCount < 1;
    },
    // Retry delay for mutations - reduced for faster feedback
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 5000),
    // Network mode for mutations
    networkMode: 'online',
  },
};

// Create query client
export const queryClient = new QueryClient({
  defaultOptions: queryConfig,
});

// Query keys factory for consistent key management
export const queryKeys = {
  // Auth queries
  auth: {
    currentUser: ['auth', 'currentUser'] as const,
    profile: ['auth', 'profile'] as const,
  },
  
  // Product queries
  products: {
    all: ['products'] as const,
    lists: () => [...queryKeys.products.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.products.lists(), { filters }] as const,
    details: () => [...queryKeys.products.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.products.details(), id] as const,
    featured: ['products', 'featured'] as const,
    related: (id: string) => ['products', 'related', id] as const,
    search: (query: string, filters: any) => ['products', 'search', { query, filters }] as const,
    categories: ['products', 'categories'] as const,
    brands: ['products', 'brands'] as const,
    tags: ['products', 'tags'] as const,
  },
  
  // Cart queries
  cart: {
    all: ['cart'] as const,
    current: ['cart', 'current'] as const,
    summary: ['cart', 'summary'] as const,
    validation: ['cart', 'validation'] as const,
    shippingOptions: ['cart', 'shipping-options'] as const,
    recommendations: ['cart', 'recommendations'] as const,
  },
  
  // Order queries
  orders: {
    all: ['orders'] as const,
    lists: () => [...queryKeys.orders.all, 'list'] as const,
    list: (filters: any) => [...queryKeys.orders.lists(), { filters }] as const,
    details: () => [...queryKeys.orders.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.orders.details(), id] as const,
    tracking: (id: string) => ['orders', 'tracking', id] as const,
    analytics: ['orders', 'analytics'] as const,
    statistics: ['orders', 'statistics'] as const,
  },
  
  // User queries
  users: {
    all: ['users'] as const,
    profile: ['users', 'profile'] as const,
    addresses: ['users', 'addresses'] as const,
    wishlist: ['users', 'wishlist'] as const,
    notifications: ['users', 'notifications'] as const,
    activityLog: ['users', 'activity-log'] as const,
    statistics: ['users', 'statistics'] as const,
    preferences: ['users', 'preferences'] as const,
    notificationSettings: ['users', 'notification-settings'] as const,
  },
  
  // Review queries
  reviews: {
    all: ['reviews'] as const,
    product: (productId: string) => ['reviews', 'product', productId] as const,
    user: ['reviews', 'user'] as const,
  },
} as const;

// Utility functions for query invalidation
export const invalidateQueries = {
  // Invalidate all auth-related queries
  auth: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.auth.all });
  },
  
  // Invalidate all product queries
  products: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
  },
  
  // Invalidate specific product
  product: (id: string) => {
    queryClient.invalidateQueries({ queryKey: queryKeys.products.detail(id) });
  },
  
  // Invalidate all cart queries
  cart: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.cart.all });
  },
  
  // Invalidate all order queries
  orders: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
  },
  
  // Invalidate specific order
  order: (id: string) => {
    queryClient.invalidateQueries({ queryKey: queryKeys.orders.detail(id) });
  },
  
  // Invalidate all user queries
  users: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
  },
  
  // Invalidate user profile
  userProfile: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.users.profile });
  },
  
  // Invalidate user addresses
  userAddresses: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.users.addresses });
  },
  
  // Invalidate user wishlist
  userWishlist: () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.users.wishlist });
  },
};

// Prefetch utilities
export const prefetchQueries = {
  // Prefetch product details
  product: async (id: string) => {
    await queryClient.prefetchQuery({
      queryKey: queryKeys.products.detail(id),
      queryFn: async () => {
        const { ProductService } = await import('@/services/api');
        const response = await ProductService.getProductById(id);
        return response.data;
      },
      staleTime: 5 * 60 * 1000, // 5 minutes
    });
  },
  
  // Prefetch user profile
  userProfile: async () => {
    await queryClient.prefetchQuery({
      queryKey: queryKeys.users.profile,
      queryFn: async () => {
        const { UserService } = await import('@/services/api');
        const response = await UserService.getCurrentUser();
        return response.data;
      },
      staleTime: 10 * 60 * 1000, // 10 minutes
    });
  },
  
  // Prefetch cart
  cart: async () => {
    await queryClient.prefetchQuery({
      queryKey: queryKeys.cart.current,
      queryFn: async () => {
        const { CartService } = await import('@/services/api');
        const response = await CartService.getCart();
        return response.data;
      },
      staleTime: 2 * 60 * 1000, // 2 minutes
    });
  },
};

// Optimistic update utilities
export const optimisticUpdates = {
  // Optimistically update cart item quantity
  updateCartItemQuantity: (itemId: string, newQuantity: number) => {
    queryClient.setQueryData(queryKeys.cart.current, (oldData: any) => {
      if (!oldData) return oldData;
      
      return {
        ...oldData,
        items: oldData.items.map((item: any) =>
          item.id === itemId ? { ...item, quantity: newQuantity } : item
        ),
      };
    });
  },
  
  // Optimistically add item to cart
  addToCart: (newItem: any) => {
    queryClient.setQueryData(queryKeys.cart.current, (oldData: any) => {
      if (!oldData) return oldData;
      
      return {
        ...oldData,
        items: [...oldData.items, newItem],
      };
    });
  },
  
  // Optimistically remove item from cart
  removeFromCart: (itemId: string) => {
    queryClient.setQueryData(queryKeys.cart.current, (oldData: any) => {
      if (!oldData) return oldData;
      
      return {
        ...oldData,
        items: oldData.items.filter((item: any) => item.id !== itemId),
      };
    });
  },
  
  // Optimistically add item to wishlist
  addToWishlist: (productId: string) => {
    queryClient.setQueryData(queryKeys.users.wishlist, (oldData: any) => {
      if (!oldData) return oldData;
      
      return {
        ...oldData,
        items: [...oldData.items, { productId, addedAt: new Date().toISOString() }],
      };
    });
  },
  
  // Optimistically remove item from wishlist
  removeFromWishlist: (productId: string) => {
    queryClient.setQueryData(queryKeys.users.wishlist, (oldData: any) => {
      if (!oldData) return oldData;
      
      return {
        ...oldData,
        items: oldData.items.filter((item: any) => item.productId !== productId),
      };
    });
  },
};

// Error handling utilities
export const handleQueryError = (error: unknown) => {
  if (error && typeof error === 'object' && 'status' in error) {
    const apiError = error as ApiError;
    
    // Handle specific error cases
    switch (apiError.status) {
      case 401:
        // Unauthorized - redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        break;
      case 403:
        // Forbidden - show access denied message
        console.warn('Access denied');
        break;
      case 404:
        // Not found - show not found message
        console.warn('Resource not found');
        break;
      case 429:
        // Rate limited - show rate limit message
        console.warn('Rate limited');
        break;
      default:
        console.error('API Error:', apiError.message);
    }
  }
};
