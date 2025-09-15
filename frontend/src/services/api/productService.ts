import { apiClient, ApiResponse } from '@/lib/axios';
import { ProductDto, CategoryDto, ReviewDto } from '@/types/api';

export interface ProductFilters {
  category?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  rating?: number;
  inStock?: boolean;
  tags?: string[];
  search?: string;
}

export interface ProductSortOptions {
  field: 'name' | 'price' | 'rating' | 'createdAt' | 'popularity';
  direction: 'asc' | 'desc';
}

export interface PaginationParams {
  page: number;
  limit: number;
}

export interface ProductListResponse {
  products: ProductDto[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export interface ProductSearchParams extends PaginationParams {
  filters?: ProductFilters;
  sort?: ProductSortOptions;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  categoryId: string;
  brand: string;
  stock: number;
  images: string[];
  specifications: Record<string, string>;
  tags: string[];
  isActive: boolean;
}

export interface UpdateProductRequest extends Partial<CreateProductRequest> {
  id: string;
}

export interface CreateReviewRequest {
  productId: string;
  rating: number;
  title: string;
  content: string;
  images?: string[];
}

export interface UpdateReviewRequest extends Partial<CreateReviewRequest> {
  id: string;
}

export class ProductService {
  private static readonly BASE_PATH = '/products';

  // Get all products with filters and pagination
  static async getProducts(params: ProductSearchParams): Promise<ApiResponse<ProductListResponse>> {
    const response = await apiClient.get(this.BASE_PATH, { params });
    return response.data;
  }

  // Get product by ID
  static async getProductById(id: string): Promise<ApiResponse<ProductDto>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  // Get featured products
  static async getFeaturedProducts(limit: number = 8): Promise<ApiResponse<ProductDto[]>> {
    const response = await apiClient.get(`${this.BASE_PATH}/featured`, {
      params: { limit }
    });
    return response.data;
  }

  // Get related products
  static async getRelatedProducts(productId: string, limit: number = 4): Promise<ApiResponse<ProductDto[]>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${productId}/related`, {
      params: { limit }
    });
    return response.data;
  }

  // Search products
  static async searchProducts(query: string, params: Omit<ProductSearchParams, 'filters'> & { filters?: Omit<ProductFilters, 'search'> }): Promise<ApiResponse<ProductListResponse>> {
    const response = await apiClient.get(`${this.BASE_PATH}/search`, {
      params: { ...params, q: query }
    });
    return response.data;
  }

  // Get product reviews
  static async getProductReviews(productId: string, params: PaginationParams): Promise<ApiResponse<{ reviews: ReviewDto[]; totalCount: number; averageRating: number }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${productId}/reviews`, { params });
    return response.data;
  }

  // Create product review
  static async createReview(data: CreateReviewRequest): Promise<ApiResponse<ReviewDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${data.productId}/reviews`, data);
    return response.data;
  }

  // Update product review
  static async updateReview(reviewId: string, data: UpdateReviewRequest): Promise<ApiResponse<ReviewDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/reviews/${reviewId}`, data);
    return response.data;
  }

  // Delete product review
  static async deleteReview(reviewId: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/reviews/${reviewId}`);
    return response.data;
  }

  // Get product categories
  static async getCategories(): Promise<ApiResponse<CategoryDto[]>> {
    const response = await apiClient.get('/categories');
    return response.data;
  }

  // Get category by ID
  static async getCategoryById(id: string): Promise<ApiResponse<CategoryDto>> {
    const response = await apiClient.get(`/categories/${id}`);
    return response.data;
  }

  // Get products by category
  static async getProductsByCategory(categoryId: string, params: Omit<ProductSearchParams, 'filters'>): Promise<ApiResponse<ProductListResponse>> {
    const response = await apiClient.get(`/categories/${categoryId}/products`, { params });
    return response.data;
  }

  // Get product brands
  static async getBrands(): Promise<ApiResponse<string[]>> {
    const response = await apiClient.get(`${this.BASE_PATH}/brands`);
    return response.data;
  }

  // Get product tags
  static async getTags(): Promise<ApiResponse<string[]>> {
    const response = await apiClient.get(`${this.BASE_PATH}/tags`);
    return response.data;
  }

  // Upload product image
  static async uploadProductImage(file: File, productId?: string): Promise<ApiResponse<{ url: string; filename: string }>> {
    const formData = new FormData();
    formData.append('file', file);
    if (productId) {
      formData.append('productId', productId);
    }

    const response = await apiClient.post(`${this.BASE_PATH}/upload-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  // Delete product image
  static async deleteProductImage(imageUrl: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/images`, {
      data: { imageUrl }
    });
    return response.data;
  }

  // Get product analytics (admin only)
  static async getProductAnalytics(productId: string): Promise<ApiResponse<{
    views: number;
    sales: number;
    revenue: number;
    reviews: number;
    averageRating: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${productId}/analytics`);
    return response.data;
  }

  // Create product (admin only)
  static async createProduct(data: CreateProductRequest): Promise<ApiResponse<ProductDto>> {
    const response = await apiClient.post(this.BASE_PATH, data);
    return response.data;
  }

  // Update product (admin only)
  static async updateProduct(data: UpdateProductRequest): Promise<ApiResponse<ProductDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/${data.id}`, data);
    return response.data;
  }

  // Delete product (admin only)
  static async deleteProduct(id: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  // Bulk update products (admin only)
  static async bulkUpdateProducts(updates: Array<{ id: string; data: Partial<UpdateProductRequest> }>): Promise<ApiResponse<{ updated: number; failed: number }>> {
    const response = await apiClient.put(`${this.BASE_PATH}/bulk`, { updates });
    return response.data;
  }
}
