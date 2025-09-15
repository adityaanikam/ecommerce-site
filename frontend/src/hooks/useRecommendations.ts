import { useQuery, useMutation } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { queryKeys } from '@/lib/react-query';
import { ProductDto } from '@/types/api';

interface RecommendationRequest {
  productId?: string;
  categoryId?: string;
  userId?: string;
  limit?: number;
  type?: 'SIMILAR' | 'FREQUENTLY_BOUGHT_TOGETHER' | 'TRENDING' | 'PERSONALIZED';
}

interface RecommendationResponse {
  products: ProductDto[];
  reason: string;
  confidence: number;
  algorithm: string;
}

interface BrowsingHistoryItem {
  productId: string;
  productName: string;
  categoryId: string;
  viewedAt: string;
  duration: number; // in seconds
}

// Hook for product recommendations
export const useRecommendations = (request: RecommendationRequest = {}) => {
  const { user, isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ['recommendations', request],
    queryFn: async (): Promise<RecommendationResponse> => {
      const params = new URLSearchParams();
      
      if (request.productId) params.append('productId', request.productId);
      if (request.categoryId) params.append('categoryId', request.categoryId);
      if (request.userId || user?.id) params.append('userId', request.userId || user?.id || '');
      if (request.limit) params.append('limit', request.limit.toString());
      if (request.type) params.append('type', request.type);

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/recommendations?${params}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch recommendations');
      }
      
      return response.json();
    },
    enabled: isAuthenticated || !!request.productId,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for similar products
export const useSimilarProducts = (productId: string, limit: number = 4) => {
  return useRecommendations({
    productId,
    type: 'SIMILAR',
    limit,
  });
};

// Hook for frequently bought together
export const useFrequentlyBoughtTogether = (productId: string, limit: number = 4) => {
  return useRecommendations({
    productId,
    type: 'FREQUENTLY_BOUGHT_TOGETHER',
    limit,
  });
};

// Hook for trending products
export const useTrendingProducts = (categoryId?: string, limit: number = 8) => {
  return useRecommendations({
    categoryId,
    type: 'TRENDING',
    limit,
  });
};

// Hook for personalized recommendations
export const usePersonalizedRecommendations = (limit: number = 8) => {
  const { user, isAuthenticated } = useAuth();
  
  return useRecommendations({
    userId: user?.id,
    type: 'PERSONALIZED',
    limit,
  });
};

// Hook for browsing history tracking
export const useBrowsingHistory = () => {
  const { user, isAuthenticated } = useAuth();

  // Track product view
  const trackProductView = useMutation({
    mutationFn: async (data: {
      productId: string;
      productName: string;
      categoryId: string;
      duration?: number;
    }) => {
      if (!isAuthenticated) return;

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/browsing-history`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            ...data,
            viewedAt: new Date().toISOString(),
          }),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to track product view');
      }

      return response.json();
    },
  });

  // Get browsing history
  const getBrowsingHistory = useQuery({
    queryKey: ['browsing-history', user?.id],
    queryFn: async (): Promise<BrowsingHistoryItem[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/browsing-history`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch browsing history');
      }
      
      return response.json();
    },
    enabled: isAuthenticated,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  // Clear browsing history
  const clearBrowsingHistory = useMutation({
    mutationFn: async () => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/browsing-history`,
        {
          method: 'DELETE',
        }
      );

      if (!response.ok) {
        throw new Error('Failed to clear browsing history');
      }

      return response.json();
    },
  });

  return {
    trackProductView: trackProductView.mutateAsync,
    browsingHistory: getBrowsingHistory.data || [],
    isLoading: getBrowsingHistory.isLoading,
    clearBrowsingHistory: clearBrowsingHistory.mutateAsync,
  };
};

// Hook for recently viewed products
export const useRecentlyViewed = (limit: number = 8) => {
  const { browsingHistory, isLoading } = useBrowsingHistory();

  const recentlyViewed = browsingHistory
    .sort((a, b) => new Date(b.viewedAt).getTime() - new Date(a.viewedAt).getTime())
    .slice(0, limit)
    .map(item => ({
      productId: item.productId,
      productName: item.productName,
      categoryId: item.categoryId,
      viewedAt: item.viewedAt,
    }));

  return {
    recentlyViewed,
    isLoading,
  };
};

// Hook for recommendation analytics (admin)
export const useRecommendationAnalytics = () => {
  return useQuery({
    queryKey: ['recommendations', 'analytics'],
    queryFn: async () => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/recommendations/analytics`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch recommendation analytics');
      }
      
      return response.json();
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Utility functions for recommendations
export const recommendationUtils = {
  // Get recommendation reason text
  getReasonText: (reason: string): string => {
    const reasonMap: Record<string, string> = {
      'SIMILAR_CATEGORY': 'Similar products in this category',
      'FREQUENTLY_BOUGHT_TOGETHER': 'Frequently bought together',
      'TRENDING': 'Trending in this category',
      'PERSONALIZED': 'Recommended for you',
      'SIMILAR_PRICE': 'Similar price range',
      'SIMILAR_BRAND': 'From the same brand',
      'CUSTOMER_ALSO_VIEWED': 'Customers also viewed',
    };
    
    return reasonMap[reason] || reason;
  },

  // Get confidence level text
  getConfidenceText: (confidence: number): string => {
    if (confidence >= 0.8) return 'Highly recommended';
    if (confidence >= 0.6) return 'Recommended';
    if (confidence >= 0.4) return 'Maybe you\'ll like';
    return 'You might like';
  },

  // Get confidence color
  getConfidenceColor: (confidence: number): string => {
    if (confidence >= 0.8) return 'text-success-600 dark:text-success-400';
    if (confidence >= 0.6) return 'text-primary-600 dark:text-primary-400';
    if (confidence >= 0.4) return 'text-warning-600 dark:text-warning-400';
    return 'text-secondary-600 dark:text-secondary-400';
  },
};

export default useRecommendations;
