import { useQuery, useInfiniteQuery, useMutation } from '@tanstack/react-query';
import { useState, useCallback, useMemo } from 'react';
import { ProductDto } from '@/types/api';

interface SearchFilters {
  categories?: string[];
  brands?: string[];
  priceRange?: {
    min: number;
    max: number;
  };
  rating?: number;
  inStock?: boolean;
  tags?: string[];
  colors?: string[];
  sizes?: string[];
  features?: string[];
}

interface SearchSort {
  field: 'relevance' | 'price' | 'rating' | 'newest' | 'popularity' | 'name';
  direction: 'asc' | 'desc';
}

interface SearchParams {
  query?: string;
  filters?: SearchFilters;
  sort?: SearchSort;
  page?: number;
  limit?: number;
}

interface SearchResult {
  products: ProductDto[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  hasNextPage: boolean;
  facets: {
    categories: Array<{ id: string; name: string; count: number }>;
    brands: Array<{ name: string; count: number }>;
    priceRanges: Array<{ min: number; max: number; count: number }>;
    ratings: Array<{ rating: number; count: number }>;
    tags: Array<{ name: string; count: number }>;
    colors: Array<{ name: string; count: number }>;
    sizes: Array<{ name: string; count: number }>;
  };
  suggestions: string[];
  searchTime: number;
}

interface SearchSuggestion {
  text: string;
  type: 'product' | 'category' | 'brand' | 'tag';
  count?: number;
}

// Hook for advanced search
export const useAdvancedSearch = (params: SearchParams) => {
  return useQuery({
    queryKey: ['search', 'advanced', params],
    queryFn: async (): Promise<SearchResult> => {
      const searchParams = new URLSearchParams();
      
      if (params.query) searchParams.append('q', params.query);
      if (params.filters) {
        Object.entries(params.filters).forEach(([key, value]) => {
          if (value) {
            if (Array.isArray(value)) {
              value.forEach(v => searchParams.append(`filter.${key}`, v.toString()));
            } else if (typeof value === 'object' && value.min !== undefined && value.max !== undefined) {
              searchParams.append(`filter.${key}.min`, value.min.toString());
              searchParams.append(`filter.${key}.max`, value.max.toString());
            } else {
              searchParams.append(`filter.${key}`, value.toString());
            }
          }
        });
      }
      if (params.sort) {
        searchParams.append('sort', params.sort.field);
        searchParams.append('order', params.sort.direction);
      }
      if (params.page) searchParams.append('page', params.page.toString());
      if (params.limit) searchParams.append('limit', params.limit.toString());

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/search/advanced?${searchParams}`
      );
      
      if (!response.ok) {
        throw new Error('Search failed');
      }
      
      return response.json();
    },
    enabled: !!params.query || !!params.filters,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for infinite search results
export const useInfiniteSearch = (params: Omit<SearchParams, 'page'>) => {
  return useInfiniteQuery({
    queryKey: ['search', 'infinite', params],
    queryFn: async ({ pageParam = 1 }): Promise<SearchResult> => {
      const searchParams = new URLSearchParams();
      
      if (params.query) searchParams.append('q', params.query);
      if (params.filters) {
        Object.entries(params.filters).forEach(([key, value]) => {
          if (value) {
            if (Array.isArray(value)) {
              value.forEach(v => searchParams.append(`filter.${key}`, v.toString()));
            } else if (typeof value === 'object' && value.min !== undefined && value.max !== undefined) {
              searchParams.append(`filter.${key}.min`, value.min.toString());
              searchParams.append(`filter.${key}.max`, value.max.toString());
            } else {
              searchParams.append(`filter.${key}`, value.toString());
            }
          }
        });
      }
      if (params.sort) {
        searchParams.append('sort', params.sort.field);
        searchParams.append('order', params.sort.direction);
      }
      searchParams.append('page', pageParam.toString());
      if (params.limit) searchParams.append('limit', params.limit.toString());

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/search/advanced?${searchParams}`
      );
      
      if (!response.ok) {
        throw new Error('Search failed');
      }
      
      return response.json();
    },
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    enabled: !!params.query || !!params.filters,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for search suggestions
export const useSearchSuggestions = (query: string) => {
  return useQuery({
    queryKey: ['search', 'suggestions', query],
    queryFn: async (): Promise<SearchSuggestion[]> => {
      if (!query || query.length < 2) return [];

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/search/suggestions?q=${encodeURIComponent(query)}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch suggestions');
      }
      
      return response.json();
    },
    enabled: !!query && query.length >= 2,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for search analytics
export const useSearchAnalytics = () => {
  return useMutation({
    mutationFn: async (data: {
      query: string;
      filters?: SearchFilters;
      resultsCount: number;
      clickedProductId?: string;
      searchTime: number;
    }) => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/search/analytics`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            ...data,
            timestamp: new Date().toISOString(),
          }),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to track search analytics');
      }

      return response.json();
    },
  });
};

// Hook for popular searches
export const usePopularSearches = (limit: number = 10) => {
  return useQuery({
    queryKey: ['search', 'popular', limit],
    queryFn: async (): Promise<Array<{ query: string; count: number }>> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/search/popular?limit=${limit}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch popular searches');
      }
      
      return response.json();
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for search filters management
export const useSearchFilters = () => {
  const [filters, setFilters] = useState<SearchFilters>({});
  const [sort, setSort] = useState<SearchSort>({ field: 'relevance', direction: 'desc' });

  const updateFilter = useCallback((key: keyof SearchFilters, value: any) => {
    setFilters(prev => ({
      ...prev,
      [key]: value,
    }));
  }, []);

  const removeFilter = useCallback((key: keyof SearchFilters) => {
    setFilters(prev => {
      const newFilters = { ...prev };
      delete newFilters[key];
      return newFilters;
    });
  }, []);

  const clearFilters = useCallback(() => {
    setFilters({});
  }, []);

  const hasActiveFilters = useMemo(() => {
    return Object.keys(filters).length > 0;
  }, [filters]);

  const getActiveFiltersCount = useMemo(() => {
    return Object.values(filters).reduce((count, value) => {
      if (Array.isArray(value)) {
        return count + value.length;
      } else if (typeof value === 'object' && value !== null) {
        return count + 1;
      } else if (value !== undefined && value !== null) {
        return count + 1;
      }
      return count;
    }, 0);
  }, [filters]);

  return {
    filters,
    sort,
    setFilters,
    setSort,
    updateFilter,
    removeFilter,
    clearFilters,
    hasActiveFilters,
    activeFiltersCount: getActiveFiltersCount,
  };
};

// Utility functions for search
export const searchUtils = {
  // Format price range for display
  formatPriceRange: (min: number, max: number): string => {
    if (min === max) return `$${min}`;
    return `$${min} - $${max}`;
  },

  // Get filter display text
  getFilterDisplayText: (key: string, value: any): string => {
    switch (key) {
      case 'priceRange':
        return `Price: ${searchUtils.formatPriceRange(value.min, value.max)}`;
      case 'rating':
        return `Rating: ${value}+ stars`;
      case 'inStock':
        return value ? 'In Stock' : 'Out of Stock';
      case 'categories':
      case 'brands':
      case 'tags':
      case 'colors':
      case 'sizes':
        return Array.isArray(value) ? value.join(', ') : value;
      default:
        return value.toString();
    }
  },

  // Get sort display text
  getSortDisplayText: (sort: SearchSort): string => {
    const fieldMap: Record<string, string> = {
      relevance: 'Relevance',
      price: 'Price',
      rating: 'Rating',
      newest: 'Newest',
      popularity: 'Popularity',
      name: 'Name',
    };

    const directionMap: Record<string, string> = {
      asc: 'Low to High',
      desc: 'High to Low',
    };

    return `${fieldMap[sort.field]} (${directionMap[sort.direction]})`;
  },

  // Build search URL
  buildSearchUrl: (query: string, filters?: SearchFilters, sort?: SearchSort): string => {
    const params = new URLSearchParams();
    
    if (query) params.append('q', query);
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value) {
          if (Array.isArray(value)) {
            value.forEach(v => params.append(`filter.${key}`, v.toString()));
          } else if (typeof value === 'object' && value.min !== undefined && value.max !== undefined) {
            params.append(`filter.${key}.min`, value.min.toString());
            params.append(`filter.${key}.max`, value.max.toString());
          } else {
            params.append(`filter.${key}`, value.toString());
          }
        }
      });
    }
    if (sort) {
      params.append('sort', sort.field);
      params.append('order', sort.direction);
    }

    return `/search?${params.toString()}`;
  },
};

export default useAdvancedSearch;
