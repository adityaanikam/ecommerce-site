import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { useNotifications } from '@/contexts/NotificationContext';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';

interface Review {
  id: string;
  productId: string;
  userId: string;
  user: {
    id: string;
    firstName: string;
    lastName: string;
    avatar?: string;
    isVerified: boolean;
  };
  rating: number;
  title: string;
  content: string;
  images?: string[];
  isVerified: boolean;
  helpfulCount: number;
  notHelpfulCount: number;
  isHelpful?: boolean; // Current user's helpful vote
  createdAt: string;
  updatedAt: string;
}

interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  ratingDistribution: {
    5: number;
    4: number;
    3: number;
    2: number;
    1: number;
  };
  verifiedReviews: number;
  withImages: number;
}

interface CreateReviewRequest {
  productId: string;
  rating: number;
  title: string;
  content: string;
  images?: string[];
}

interface UpdateReviewRequest {
  reviewId: string;
  rating?: number;
  title?: string;
  content?: string;
  images?: string[];
}

interface ReviewFilters {
  rating?: number;
  verified?: boolean;
  withImages?: boolean;
  sortBy?: 'newest' | 'oldest' | 'helpful' | 'rating';
}

// Hook for product reviews
export const useProductReviews = (productId: string, filters?: ReviewFilters) => {
  return useQuery({
    queryKey: ['reviews', 'product', productId, filters],
    queryFn: async (): Promise<{ reviews: Review[]; stats: ReviewStats }> => {
      const params = new URLSearchParams();
      if (filters?.rating) params.append('rating', filters.rating.toString());
      if (filters?.verified !== undefined) params.append('verified', filters.verified.toString());
      if (filters?.withImages !== undefined) params.append('withImages', filters.withImages.toString());
      if (filters?.sortBy) params.append('sortBy', filters.sortBy);

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/products/${productId}/reviews?${params}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch reviews');
      }
      
      return response.json();
    },
    enabled: !!productId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for user's reviews
export const useUserReviews = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ['reviews', 'user'],
    queryFn: async (): Promise<Review[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/reviews`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch user reviews');
      }
      
      return response.json();
    },
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for review management
export const useReviewManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Create review
  const createReviewMutation = useMutation({
    mutationFn: async (data: CreateReviewRequest): Promise<Review> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/reviews`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to create review');
      }
      
      return response.json();
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['reviews', 'product', variables.productId] });
      queryClient.invalidateQueries({ queryKey: ['reviews', 'user'] });
      invalidateQueries.products();
      showSuccess('Review submitted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Update review
  const updateReviewMutation = useMutation({
    mutationFn: async (data: UpdateReviewRequest): Promise<Review> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/reviews/${data.reviewId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to update review');
      }
      
      return response.json();
    },
    onSuccess: (review) => {
      queryClient.invalidateQueries({ queryKey: ['reviews', 'product', review.productId] });
      queryClient.invalidateQueries({ queryKey: ['reviews', 'user'] });
      showSuccess('Review updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Delete review
  const deleteReviewMutation = useMutation({
    mutationFn: async (reviewId: string): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/reviews/${reviewId}`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to delete review');
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
      showSuccess('Review deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    createReview: createReviewMutation.mutateAsync,
    updateReview: updateReviewMutation.mutateAsync,
    deleteReview: deleteReviewMutation.mutateAsync,
    isCreating: createReviewMutation.isPending,
    isUpdating: updateReviewMutation.isPending,
    isDeleting: deleteReviewMutation.isPending,
  };
};

// Hook for review helpfulness
export const useReviewHelpfulness = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  const voteHelpfulMutation = useMutation({
    mutationFn: async ({ reviewId, isHelpful }: { reviewId: string; isHelpful: boolean }): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/reviews/${reviewId}/helpful`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ isHelpful }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to vote on review');
      }
    },
    onSuccess: (_, { reviewId }) => {
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
      showSuccess('Thank you for your feedback');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    voteHelpful: voteHelpfulMutation.mutateAsync,
    isVoting: voteHelpfulMutation.isPending,
  };
};

// Hook for review images
export const useReviewImages = () => {
  const { showSuccess, showError } = useNotifications();

  const uploadImageMutation = useMutation({
    mutationFn: async (file: File): Promise<{ url: string; filename: string }> => {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/reviews/upload-image`,
        {
          method: 'POST',
          body: formData,
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to upload image');
      }
      
      return response.json();
    },
    onSuccess: () => {
      showSuccess('Image uploaded successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    uploadImage: uploadImageMutation.mutateAsync,
    isUploading: uploadImageMutation.isPending,
  };
};

// Q&A System
interface Question {
  id: string;
  productId: string;
  userId: string;
  user: {
    id: string;
    firstName: string;
    lastName: string;
    avatar?: string;
  };
  question: string;
  answers: Answer[];
  isAnswered: boolean;
  helpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

interface Answer {
  id: string;
  questionId: string;
  userId: string;
  user: {
    id: string;
    firstName: string;
    lastName: string;
    avatar?: string;
    isVerified: boolean;
  };
  answer: string;
  isVerified: boolean;
  helpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

interface CreateQuestionRequest {
  productId: string;
  question: string;
}

interface CreateAnswerRequest {
  questionId: string;
  answer: string;
}

// Hook for product Q&A
export const useProductQA = (productId: string) => {
  return useQuery({
    queryKey: ['qa', 'product', productId],
    queryFn: async (): Promise<Question[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/products/${productId}/questions`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch questions');
      }
      
      return response.json();
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for Q&A management
export const useQAManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Ask question
  const askQuestionMutation = useMutation({
    mutationFn: async (data: CreateQuestionRequest): Promise<Question> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/questions`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to ask question');
      }
      
      return response.json();
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['qa', 'product', variables.productId] });
      showSuccess('Question submitted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Answer question
  const answerQuestionMutation = useMutation({
    mutationFn: async (data: CreateAnswerRequest): Promise<Answer> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/questions/${data.questionId}/answers`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ answer: data.answer }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to answer question');
      }
      
      return response.json();
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['qa'] });
      showSuccess('Answer submitted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    askQuestion: askQuestionMutation.mutateAsync,
    answerQuestion: answerQuestionMutation.mutateAsync,
    isAsking: askQuestionMutation.isPending,
    isAnswering: answerQuestionMutation.isPending,
  };
};

// Utility functions for reviews
export const reviewUtils = {
  // Get rating stars
  getRatingStars: (rating: number): string => {
    return '★'.repeat(Math.floor(rating)) + '☆'.repeat(5 - Math.floor(rating));
  },

  // Get rating color
  getRatingColor: (rating: number): string => {
    if (rating >= 4) return 'text-success-600 dark:text-success-400';
    if (rating >= 3) return 'text-warning-600 dark:text-warning-400';
    if (rating >= 2) return 'text-orange-600 dark:text-orange-400';
    return 'text-error-600 dark:text-error-400';
  },

  // Get rating text
  getRatingText: (rating: number): string => {
    if (rating >= 4.5) return 'Excellent';
    if (rating >= 4) return 'Very Good';
    if (rating >= 3.5) return 'Good';
    if (rating >= 3) return 'Average';
    if (rating >= 2.5) return 'Below Average';
    if (rating >= 2) return 'Poor';
    return 'Very Poor';
  },

  // Format review date
  formatReviewDate: (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
    
    if (diffInDays === 0) return 'Today';
    if (diffInDays === 1) return 'Yesterday';
    if (diffInDays < 7) return `${diffInDays} days ago`;
    if (diffInDays < 30) return `${Math.floor(diffInDays / 7)} weeks ago`;
    if (diffInDays < 365) return `${Math.floor(diffInDays / 30)} months ago`;
    return `${Math.floor(diffInDays / 365)} years ago`;
  },

  // Get helpfulness percentage
  getHelpfulnessPercentage: (helpful: number, notHelpful: number): number => {
    const total = helpful + notHelpful;
    if (total === 0) return 0;
    return Math.round((helpful / total) * 100);
  },
};

export default useReviews;
