import { apiService } from './api';
import {
  Product,
  ProductSearchRequest,
  CreateProductRequest,
  UpdateProductRequest,
  ProductAnalytics,
  PaginatedResponse,
  ApiResponse,
} from '@/types';

export const productService = {
  // Get all products
  getProducts: async (page = 0, size = 20): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get product by ID
  getProduct: async (id: string): Promise<Product> => {
    const response = await apiService.get<Product>(`/products/${id}`);
    return response.data!;
  },

  // Search products
  searchProducts: async (query: string, page = 0, size = 20): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Advanced search with filters
  searchProductsWithFilters: async (
    searchRequest: ProductSearchRequest,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.post<PaginatedResponse<Product>>(
      `/products/search/advanced?page=${page}&size=${size}`,
      searchRequest
    );
    return response.data!;
  },

  // Get products by category
  getProductsByCategory: async (
    categoryId: string,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products/category/${categoryId}?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get products by brand
  getProductsByBrand: async (
    brand: string,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products/brand/${encodeURIComponent(brand)}?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get products by price range
  getProductsByPriceRange: async (
    minPrice: number,
    maxPrice: number,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products/price-range?minPrice=${minPrice}&maxPrice=${maxPrice}&page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get featured products
  getFeaturedProducts: async (limit = 10): Promise<Product[]> => {
    const response = await apiService.get<Product[]>(`/products/featured?limit=${limit}`);
    return response.data!;
  },

  // Get top rated products
  getTopRatedProducts: async (limit = 10): Promise<Product[]> => {
    const response = await apiService.get<Product[]>(`/products/top-rated?limit=${limit}`);
    return response.data!;
  },

  // Create product (Admin/Seller)
  createProduct: async (productData: CreateProductRequest): Promise<Product> => {
    const response = await apiService.post<Product>('/products', productData);
    return response.data!;
  },

  // Update product (Admin/Seller)
  updateProduct: async (id: string, productData: UpdateProductRequest): Promise<Product> => {
    const response = await apiService.put<Product>(`/products/${id}`, productData);
    return response.data!;
  },

  // Delete product (Admin)
  deleteProduct: async (id: string): Promise<void> => {
    await apiService.delete(`/products/${id}`);
  },

  // Upload product images
  uploadProductImages: async (id: string, files: File[]): Promise<Product> => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });
    
    const response = await apiService.upload<Product>(`/products/${id}/images`, formData);
    return response.data!;
  },

  // Remove product image
  removeProductImage: async (id: string, imageUrl: string): Promise<Product> => {
    const response = await apiService.delete<Product>(`/products/${id}/images?imageUrl=${encodeURIComponent(imageUrl)}`);
    return response.data!;
  },

  // Update stock
  updateStock: async (id: string, stock: number): Promise<Product> => {
    const response = await apiService.put<Product>(`/products/${id}/stock?stock=${stock}`);
    return response.data!;
  },

  // Reduce stock
  reduceStock: async (id: string, quantity: number): Promise<Product> => {
    const response = await apiService.post<Product>(`/products/${id}/stock/reduce?quantity=${quantity}`);
    return response.data!;
  },

  // Increase stock
  increaseStock: async (id: string, quantity: number): Promise<Product> => {
    const response = await apiService.post<Product>(`/products/${id}/stock/increase?quantity=${quantity}`);
    return response.data!;
  },

  // Get low stock products
  getLowStockProducts: async (threshold = 10): Promise<Product[]> => {
    const response = await apiService.get<Product[]>(`/products/low-stock?threshold=${threshold}`);
    return response.data!;
  },

  // Get out of stock products
  getOutOfStockProducts: async (): Promise<Product[]> => {
    const response = await apiService.get<Product[]>('/products/out-of-stock');
    return response.data!;
  },

  // Get product analytics
  getProductAnalytics: async (id: string): Promise<ProductAnalytics> => {
    const response = await apiService.get<ProductAnalytics>(`/products/${id}/analytics`);
    return response.data!;
  },

  // Get products by seller
  getProductsBySeller: async (
    sellerId: string,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<Product>> => {
    const response = await apiService.get<PaginatedResponse<Product>>(
      `/products/seller/${sellerId}?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Check product availability
  checkProductAvailability: async (id: string, quantity: number): Promise<{
    available: boolean;
    price: number;
    productId: string;
    requestedQuantity: number;
  }> => {
    const response = await apiService.get<{
      available: boolean;
      price: number;
      productId: string;
      requestedQuantity: number;
    }>(`/products/${id}/available?quantity=${quantity}`);
    return response.data!;
  },
};
