import axios from '@/lib/axios';
import { Product } from '@/types/api';

import { API_BASE_URL } from '@/config';

export interface ProductResponse {
  content: Product[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ProductFilters {
  category?: string;
  subcategory?: string;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'price' | 'name' | 'rating';
  sortOrder?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export const productService = {
  async getProducts(filters: ProductFilters = {}): Promise<ProductResponse> {
    try {
      const params = new URLSearchParams();
      
      if (filters.category) params.append('category', filters.category);
      if (filters.subcategory) params.append('subcategory', filters.subcategory);
      if (filters.search) params.append('search', filters.search);
      if (filters.minPrice !== undefined) params.append('minPrice', filters.minPrice.toString());
      if (filters.maxPrice !== undefined) params.append('maxPrice', filters.maxPrice.toString());
      if (filters.sortBy) params.append('sortBy', filters.sortBy);
      if (filters.sortOrder) params.append('sortOrder', filters.sortOrder);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());

      const response = await axios.get(`${API_BASE_URL}/products?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching products:', error instanceof Error ? error.message : String(error));
      // Return empty response instead of throwing
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 0,
        number: 0,
        first: true,
        last: true,
        numberOfElements: 0,
        empty: true
      };
    }
  },

  async getProductById(id: string): Promise<Product> {
    try {
      const response = await axios.get(`${API_BASE_URL}/products/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching product ${id}:`, error instanceof Error ? error.message : String(error));
      throw error; // Re-throw for product details as it's critical
    }
  },

  async getCategories(): Promise<any[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories`);
      return response.data || [];
    } catch (error) {
      console.error('Error fetching categories:', error instanceof Error ? error.message : String(error));
      return [];
    }
  }
};