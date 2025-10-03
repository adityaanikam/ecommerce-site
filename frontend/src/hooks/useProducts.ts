import { useQuery, useMutation, useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { ProductService } from '@/services/api/index';
import type { ProductSearchParams, ProductFilters, ProductSortOptions } from '@/services/api/productService';
import { ProductDto, CategoryDto, ReviewDto } from '@/types/api';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';
import { useNotifications } from '@/contexts/NotificationContext';

// Hook for getting products with pagination
export const useProducts = (params: ProductSearchParams) => {
  return useQuery({
    queryKey: queryKeys.products.list(params.filters || {}),
    queryFn: async () => {
      const response = await ProductService.getProducts(params);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    keepPreviousData: true,
  });
};

// Hook for infinite scrolling products
export const useInfiniteProducts = (filters?: ProductFilters, sort?: ProductSortOptions) => {
  return useInfiniteQuery({
    queryKey: queryKeys.products.list(filters || {}),
    initialPageParam: 1,
    queryFn: async ({ pageParam }) => {
      const response = await ProductService.getProducts({
        page: pageParam,
        limit: 20,
        filters,
        sort,
      });
      return response.data;
    },
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for getting a single product
export const useProduct = (id: string) => {
  return useQuery({
    queryKey: queryKeys.products.detail(id),
    queryFn: async () => {
      const response = await ProductService.getProductById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for getting featured products
export const useFeaturedProducts = (limit: number = 8) => {
  return useQuery({
    queryKey: queryKeys.products.featured,
    queryFn: async () => {
      const response = await ProductService.getFeaturedProducts(limit);
      return response.data;
    },
    staleTime: 15 * 60 * 1000, // 15 minutes
  });
};

// Hook for getting related products
export const useRelatedProducts = (productId: string, limit: number = 4) => {
  return useQuery({
    queryKey: queryKeys.products.related(productId),
    queryFn: async () => {
      const response = await ProductService.getRelatedProducts(productId, limit);
      return response.data;
    },
    enabled: !!productId,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for searching products
export const useSearchProducts = (query: string, params: Omit<ProductSearchParams, 'filters'> & { filters?: Omit<ProductFilters, 'search'> }) => {
  return useQuery({
    queryKey: queryKeys.products.search(query, params.filters || {}),
    queryFn: async () => {
      const response = await ProductService.searchProducts(query, params);
      return response.data;
    },
    enabled: !!query && query.length >= 2,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for getting product reviews
export const useProductReviews = (productId: string, page: number = 1, limit: number = 10) => {
  return useQuery({
    queryKey: queryKeys.reviews.product(productId),
    queryFn: async () => {
      const response = await ProductService.getProductReviews(productId, { page, limit });
      return response.data;
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for creating a product review
export const useCreateReview = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.createReview,
    onSuccess: (response, variables) => {
      // Invalidate product reviews
      queryClient.invalidateQueries({
        queryKey: queryKeys.reviews.product(variables.productId),
      });
      
      // Invalidate product details to update rating
      queryClient.invalidateQueries({
        queryKey: queryKeys.products.detail(variables.productId),
      });
      
      showSuccess('Review submitted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for updating a product review
export const useUpdateReview = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.updateReview,
    onSuccess: (response, variables) => {
      // Invalidate product reviews
      queryClient.invalidateQueries({
        queryKey: queryKeys.reviews.product(variables.productId || ''),
      });
      
      showSuccess('Review updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for deleting a product review
export const useDeleteReview = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.deleteReview,
    onSuccess: (response, reviewId) => {
      // Invalidate all review queries
      queryClient.invalidateQueries({
        queryKey: queryKeys.reviews.all,
      });
      
      showSuccess('Review deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting categories
export const useCategories = () => {
  return useQuery({
    queryKey: queryKeys.products.categories,
    queryFn: async () => {
      const response = await ProductService.getCategories();
      return response.data;
    },
    staleTime: 30 * 60 * 1000, // 30 minutes
  });
};

// Hook for getting a single category
export const useCategory = (id: string) => {
  return useQuery({
    queryKey: ['categories', id],
    queryFn: async () => {
      const response = await ProductService.getCategoryById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 15 * 60 * 1000, // 15 minutes
  });
};

// Hook for getting products by category
export const useProductsByCategory = (categoryId: string, params: Omit<ProductSearchParams, 'filters'>) => {
  return useQuery({
    queryKey: ['categories', categoryId, 'products', params],
    queryFn: async () => {
      const response = await ProductService.getProductsByCategory(categoryId, params);
      return response.data;
    },
    enabled: !!categoryId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for getting brands
export const useBrands = () => {
  return useQuery({
    queryKey: queryKeys.products.brands,
    queryFn: async () => {
      const response = await ProductService.getBrands();
      return response.data;
    },
    staleTime: 30 * 60 * 1000, // 30 minutes
  });
};

// Hook for getting tags
export const useTags = () => {
  return useQuery({
    queryKey: queryKeys.products.tags,
    queryFn: async () => {
      const response = await ProductService.getTags();
      return response.data;
    },
    staleTime: 30 * 60 * 1000, // 30 minutes
  });
};

// Hook for uploading product image
export const useUploadProductImage = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.uploadProductImage,
    onSuccess: () => {
      showSuccess('Image uploaded successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for deleting product image
export const useDeleteProductImage = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.deleteProductImage,
    onSuccess: () => {
      showSuccess('Image deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting product analytics (admin only)
export const useProductAnalytics = (productId: string) => {
  return useQuery({
    queryKey: ['products', productId, 'analytics'],
    queryFn: async () => {
      const response = await ProductService.getProductAnalytics(productId);
      return response.data;
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for creating a product (admin only)
export const useCreateProduct = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.createProduct,
    onSuccess: () => {
      // Invalidate products list
      invalidateQueries.products();
      showSuccess('Product created successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for updating a product (admin only)
export const useUpdateProduct = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.updateProduct,
    onSuccess: (response, variables) => {
      // Invalidate specific product and products list
      invalidateQueries.product(variables.id);
      invalidateQueries.products();
      showSuccess('Product updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for deleting a product (admin only)
export const useDeleteProduct = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ProductService.deleteProduct,
    onSuccess: (response, productId) => {
      // Remove from cache and invalidate lists
      queryClient.removeQueries({
        queryKey: queryKeys.products.detail(productId),
      });
      invalidateQueries.products();
      showSuccess('Product deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};
