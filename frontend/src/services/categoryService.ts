import axios from '@/lib/axios';
import { API_BASE_URL, CATEGORY_IMAGES, CATEGORY_DESCRIPTIONS } from '@/config';

export interface Category {
  name: string;
  slug: string;
  description: string;
  image: string;
  subcategories: {
    name: string;
    slug: string;
    count: number;
  }[];
}

export const categoryService = {
  getCategories: async (): Promise<Category[]> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories`);
      
      // Check if response data is valid
      if (!response.data || !Array.isArray(response.data)) {
        throw new Error('Invalid response format from categories API');
      }
      
      // Transform the response to include images and descriptions
      return response.data.map((category: any) => ({
        ...category,
        image: CATEGORY_IMAGES[category.name] || 'https://placehold.co/800x600/6366f1/ffffff',
        description: CATEGORY_DESCRIPTIONS[category.name] || '',
      }));
    } catch (error) {
      console.error('Error fetching categories:', error instanceof Error ? error.message : String(error));
      // Return empty array instead of throwing to prevent crashes
      return [];
    }
  },

  getCategoryBySlug: async (slug: string): Promise<Category> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories/${slug}`);
      const category = response.data;
      
      if (!category) {
        throw new Error(`Category with slug ${slug} not found`);
      }
      
      return {
        ...category,
        image: CATEGORY_IMAGES[category.name] || 'https://placehold.co/800x600/6366f1/ffffff',
        description: CATEGORY_DESCRIPTIONS[category.name] || '',
      };
    } catch (error) {
      console.error(`Error fetching category ${slug}:`, error instanceof Error ? error.message : String(error));
      // Return a default category instead of throwing
      return {
        name: 'Unknown Category',
        slug: slug,
        description: 'Category not found',
        image: 'https://placehold.co/800x600/6366f1/ffffff',
        subcategories: []
      };
    }
  },

  getCategoryStats: async (): Promise<{ [key: string]: number }> => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories/stats`);
      return response.data || {};
    } catch (error) {
      console.error('Error fetching category stats:', error instanceof Error ? error.message : String(error));
      // Return empty object instead of throwing
      return {};
    }
  }
};
