import { useState, useCallback, useMemo } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useNotifications } from '@/contexts/NotificationContext';
import { ProductDto } from '@/types/api';
import { useProducts } from './useProducts';

interface ComparisonProduct extends ProductDto {
  comparisonId: string;
  addedAt: string;
}

interface ComparisonFeature {
  name: string;
  values: Record<string, string | number | boolean>;
}

interface ComparisonSpecification {
  category: string;
  features: ComparisonFeature[];
}

interface ComparisonSummary {
  totalProducts: number;
  priceRange: {
    min: number;
    max: number;
    average: number;
  };
  ratingRange: {
    min: number;
    max: number;
    average: number;
  };
  bestValue: string; // productId
  highestRated: string; // productId
  cheapest: string; // productId
}

// Hook for product comparison
export const useProductComparison = () => {
  const [comparisonProducts, setComparisonProducts] = useState<ComparisonProduct[]>([]);
  const { showSuccess, showError } = useNotifications();

  const MAX_COMPARISON_ITEMS = 4;

  // Add product to comparison
  const addToComparison = useCallback((product: ProductDto) => {
    setComparisonProducts(prev => {
      // Check if product is already in comparison
      if (prev.some(p => p.id === product.id)) {
        showError('Product is already in comparison');
        return prev;
      }

      // Check if comparison is full
      if (prev.length >= MAX_COMPARISON_ITEMS) {
        showError(`You can compare up to ${MAX_COMPARISON_ITEMS} products`);
        return prev;
      }

      const newProduct: ComparisonProduct = {
        ...product,
        comparisonId: `${product.id}-${Date.now()}`,
        addedAt: new Date().toISOString(),
      };

      showSuccess('Product added to comparison');
      return [...prev, newProduct];
    });
  }, [showSuccess, showError]);

  // Remove product from comparison
  const removeFromComparison = useCallback((productId: string) => {
    setComparisonProducts(prev => {
      const newProducts = prev.filter(p => p.id !== productId);
      showSuccess('Product removed from comparison');
      return newProducts;
    });
  }, [showSuccess]);

  // Clear all products from comparison
  const clearComparison = useCallback(() => {
    setComparisonProducts([]);
    showSuccess('Comparison cleared');
  }, [showSuccess]);

  // Check if product is in comparison
  const isInComparison = useCallback((productId: string): boolean => {
    return comparisonProducts.some(p => p.id === productId);
  }, [comparisonProducts]);

  // Get comparison summary
  const comparisonSummary = useMemo((): ComparisonSummary | null => {
    if (comparisonProducts.length === 0) return null;

    const prices = comparisonProducts.map(p => p.price);
    const ratings = comparisonProducts.map(p => p.rating || 0);

    const priceRange = {
      min: Math.min(...prices),
      max: Math.max(...prices),
      average: prices.reduce((sum, price) => sum + price, 0) / prices.length,
    };

    const ratingRange = {
      min: Math.min(...ratings),
      max: Math.max(...ratings),
      average: ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length,
    };

    // Find best value (highest rating/price ratio)
    const bestValue = comparisonProducts.reduce((best, current) => {
      const bestRatio = (best.rating || 0) / best.price;
      const currentRatio = (current.rating || 0) / current.price;
      return currentRatio > bestRatio ? current : best;
    }).id;

    // Find highest rated
    const highestRated = comparisonProducts.reduce((best, current) => {
      return (current.rating || 0) > (best.rating || 0) ? current : best;
    }).id;

    // Find cheapest
    const cheapest = comparisonProducts.reduce((best, current) => {
      return current.price < best.price ? current : best;
    }).id;

    return {
      totalProducts: comparisonProducts.length,
      priceRange,
      ratingRange,
      bestValue,
      highestRated,
      cheapest,
    };
  }, [comparisonProducts]);

  // Get comparison specifications
  const comparisonSpecifications = useMemo((): ComparisonSpecification[] => {
    if (comparisonProducts.length === 0) return [];

    // Get all unique specification categories
    const categories = new Set<string>();
    comparisonProducts.forEach(product => {
      if (product.specifications) {
        Object.keys(product.specifications).forEach(key => {
          categories.add(key);
        });
      }
    });

    // Build comparison specifications
    const specifications: ComparisonSpecification[] = [];
    
    categories.forEach(category => {
      const features: ComparisonFeature[] = [];
      
      // Get all unique feature names in this category
      const featureNames = new Set<string>();
      comparisonProducts.forEach(product => {
        if (product.specifications && product.specifications[category]) {
          Object.keys(product.specifications[category]).forEach(featureName => {
            featureNames.add(featureName);
          });
        }
      });

      featureNames.forEach(featureName => {
        const values: Record<string, string | number | boolean> = {};
        
        comparisonProducts.forEach(product => {
          const value = product.specifications?.[category]?.[featureName] || 'N/A';
          values[product.id] = value;
        });

        features.push({
          name: featureName,
          values,
        });
      });

      if (features.length > 0) {
        specifications.push({
          category,
          features,
        });
      }
    });

    return specifications;
  }, [comparisonProducts]);

  // Get comparison features (common features across all products)
  const comparisonFeatures = useMemo(() => {
    if (comparisonProducts.length === 0) return [];

    const features = [
      { name: 'Price', key: 'price', format: (value: any) => `$${value}` },
      { name: 'Rating', key: 'rating', format: (value: any) => value ? `${value}/5` : 'No rating' },
      { name: 'Reviews', key: 'reviewCount', format: (value: any) => value ? `${value} reviews` : 'No reviews' },
      { name: 'Brand', key: 'brand', format: (value: any) => value || 'N/A' },
      { name: 'Stock', key: 'stock', format: (value: any) => value > 0 ? `${value} in stock` : 'Out of stock' },
      { name: 'SKU', key: 'sku', format: (value: any) => value || 'N/A' },
    ];

    return features.map(feature => ({
      name: feature.name,
      values: comparisonProducts.reduce((acc, product) => {
        acc[product.id] = feature.format(product[feature.key as keyof ProductDto]);
        return acc;
      }, {} as Record<string, string>),
    }));
  }, [comparisonProducts]);

  return {
    comparisonProducts,
    addToComparison,
    removeFromComparison,
    clearComparison,
    isInComparison,
    comparisonSummary,
    comparisonSpecifications,
    comparisonFeatures,
    canAddMore: comparisonProducts.length < MAX_COMPARISON_ITEMS,
    isEmpty: comparisonProducts.length === 0,
  };
};

// Hook for comparison analytics
export const useComparisonAnalytics = () => {
  return useMutation({
    mutationFn: async (data: {
      productIds: string[];
      comparisonDuration: number;
      viewedSpecifications: string[];
      action: 'VIEW' | 'ADD_TO_CART' | 'REMOVE' | 'CLEAR';
    }) => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/analytics/comparison`,
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
        throw new Error('Failed to track comparison analytics');
      }

      return response.json();
    },
  });
};

// Hook for comparison recommendations
export const useComparisonRecommendations = (productIds: string[]) => {
  return useQuery({
    queryKey: ['comparison', 'recommendations', productIds],
    queryFn: async (): Promise<ProductDto[]> => {
      if (productIds.length === 0) return [];

      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/comparison/recommendations?productIds=${productIds.join(',')}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch comparison recommendations');
      }
      
      return response.json();
    },
    enabled: productIds.length > 0,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Utility functions for comparison
export const comparisonUtils = {
  // Format comparison value
  formatValue: (value: any, type: 'price' | 'rating' | 'text' | 'number' = 'text'): string => {
    if (value === null || value === undefined) return 'N/A';
    
    switch (type) {
      case 'price':
        return `$${value}`;
      case 'rating':
        return `${value}/5`;
      case 'number':
        return value.toString();
      default:
        return value.toString();
    }
  },

  // Get comparison winner
  getWinner: (values: Record<string, any>, type: 'higher' | 'lower' = 'higher'): string | null => {
    const entries = Object.entries(values);
    if (entries.length === 0) return null;

    return entries.reduce((winner, [key, value]) => {
      if (winner === null) return key;
      
      const currentValue = parseFloat(value) || 0;
      const winnerValue = parseFloat(values[winner]) || 0;
      
      if (type === 'higher') {
        return currentValue > winnerValue ? key : winner;
      } else {
        return currentValue < winnerValue ? key : winner;
      }
    }, null as string | null);
  },

  // Generate comparison summary text
  generateSummaryText: (summary: ComparisonSummary): string => {
    const { totalProducts, priceRange, ratingRange } = summary;
    
    let text = `Comparing ${totalProducts} products. `;
    text += `Price range: $${priceRange.min} - $${priceRange.max}. `;
    text += `Rating range: ${ratingRange.min}/5 - ${ratingRange.max}/5.`;
    
    return text;
  },

  // Export comparison data
  exportComparison: (products: ComparisonProduct[], format: 'json' | 'csv' = 'json'): string => {
    if (format === 'json') {
      return JSON.stringify(products, null, 2);
    }
    
    // CSV format
    const headers = ['Name', 'Price', 'Rating', 'Brand', 'Stock', 'SKU'];
    const rows = products.map(product => [
      product.name,
      product.price,
      product.rating || 'N/A',
      product.brand,
      product.stock,
      product.sku || 'N/A',
    ]);
    
    return [headers, ...rows].map(row => row.join(',')).join('\n');
  },
};

export default useProductComparison;
