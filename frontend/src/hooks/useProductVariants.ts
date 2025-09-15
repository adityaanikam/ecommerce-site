import { useState, useMemo, useCallback } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useNotifications } from '@/contexts/NotificationContext';
import { ProductDto } from '@/types/api';

interface ProductVariant {
  id: string;
  productId: string;
  name: string;
  type: 'SIZE' | 'COLOR' | 'STYLE' | 'MATERIAL' | 'CUSTOM';
  value: string;
  displayValue: string;
  priceModifier: number; // Additional cost for this variant
  stock: number;
  sku: string;
  images?: string[];
  isDefault: boolean;
  isAvailable: boolean;
}

interface VariantOption {
  type: string;
  value: string;
  displayValue: string;
  priceModifier: number;
  stock: number;
  sku: string;
  images?: string[];
  isDefault: boolean;
  isAvailable: boolean;
}

interface SelectedVariants {
  [variantType: string]: string; // type -> value
}

interface VariantCombination {
  variants: SelectedVariants;
  price: number;
  stock: number;
  sku: string;
  images?: string[];
  isAvailable: boolean;
}

// Hook for product variants
export const useProductVariants = (productId: string) => {
  const [selectedVariants, setSelectedVariants] = useState<SelectedVariants>({});

  const { data: variants, isLoading, error } = useQuery({
    queryKey: ['product-variants', productId],
    queryFn: async (): Promise<ProductVariant[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/products/${productId}/variants`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch product variants');
      }
      
      return response.json();
    },
    enabled: !!productId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Group variants by type
  const variantGroups = useMemo(() => {
    if (!variants) return {};

    return variants.reduce((groups, variant) => {
      if (!groups[variant.type]) {
        groups[variant.type] = [];
      }
      groups[variant.type].push(variant);
      return groups;
    }, {} as Record<string, ProductVariant[]>);
  }, [variants]);

  // Get available variant options for each type
  const availableOptions = useMemo(() => {
    if (!variants) return {};

    const options: Record<string, VariantOption[]> = {};
    
    Object.entries(variantGroups).forEach(([type, typeVariants]) => {
      options[type] = typeVariants.map(variant => ({
        type: variant.type,
        value: variant.value,
        displayValue: variant.displayValue,
        priceModifier: variant.priceModifier,
        stock: variant.stock,
        sku: variant.sku,
        images: variant.images,
        isDefault: variant.isDefault,
        isAvailable: variant.isAvailable,
      }));
    });

    return options;
  }, [variantGroups]);

  // Get current variant combination
  const currentCombination = useMemo((): VariantCombination | null => {
    if (!variants || Object.keys(selectedVariants).length === 0) return null;

    // Find the specific variant combination
    const selectedVariantObjects = Object.entries(selectedVariants).map(([type, value]) =>
      variants.find(v => v.type === type && v.value === value)
    ).filter(Boolean) as ProductVariant[];

    if (selectedVariantObjects.length === 0) return null;

    const basePrice = variants[0]?.priceModifier || 0; // Assuming first variant has base price
    const totalPriceModifier = selectedVariantObjects.reduce((sum, variant) => sum + variant.priceModifier, 0);
    const minStock = Math.min(...selectedVariantObjects.map(v => v.stock));
    const combinedSku = selectedVariantObjects.map(v => v.sku).join('-');
    const allImages = selectedVariantObjects.flatMap(v => v.images || []);
    const isAvailable = selectedVariantObjects.every(v => v.isAvailable) && minStock > 0;

    return {
      variants: selectedVariants,
      price: basePrice + totalPriceModifier,
      stock: minStock,
      sku: combinedSku,
      images: allImages.length > 0 ? allImages : undefined,
      isAvailable,
    };
  }, [variants, selectedVariants]);

  // Select variant
  const selectVariant = useCallback((type: string, value: string) => {
    setSelectedVariants(prev => ({
      ...prev,
      [type]: value,
    }));
  }, []);

  // Clear variant selection
  const clearVariants = useCallback(() => {
    setSelectedVariants({});
  }, []);

  // Get default variants
  const getDefaultVariants = useCallback(() => {
    if (!variants) return {};

    const defaults: SelectedVariants = {};
    Object.entries(variantGroups).forEach(([type, typeVariants]) => {
      const defaultVariant = typeVariants.find(v => v.isDefault);
      if (defaultVariant) {
        defaults[type] = defaultVariant.value;
      }
    });
    return defaults;
  }, [variants, variantGroups]);

  // Initialize with default variants
  const initializeDefaults = useCallback(() => {
    const defaults = getDefaultVariants();
    setSelectedVariants(defaults);
  }, [getDefaultVariants]);

  // Check if variant is selected
  const isVariantSelected = useCallback((type: string, value: string): boolean => {
    return selectedVariants[type] === value;
  }, [selectedVariants]);

  // Get variant display name
  const getVariantDisplayName = useCallback((type: string): string => {
    const typeMap: Record<string, string> = {
      SIZE: 'Size',
      COLOR: 'Color',
      STYLE: 'Style',
      MATERIAL: 'Material',
      CUSTOM: 'Option',
    };
    return typeMap[type] || type;
  }, []);

  // Get variant color (for color variants)
  const getVariantColor = useCallback((value: string): string => {
    const colorMap: Record<string, string> = {
      'red': '#ef4444',
      'blue': '#3b82f6',
      'green': '#22c55e',
      'yellow': '#eab308',
      'purple': '#a855f7',
      'pink': '#ec4899',
      'orange': '#f97316',
      'black': '#000000',
      'white': '#ffffff',
      'gray': '#6b7280',
      'brown': '#a3a3a3',
    };
    return colorMap[value.toLowerCase()] || value;
  }, []);

  return {
    variants,
    variantGroups,
    availableOptions,
    selectedVariants,
    currentCombination,
    isLoading,
    error,
    selectVariant,
    clearVariants,
    initializeDefaults,
    isVariantSelected,
    getVariantDisplayName,
    getVariantColor,
  };
};

// Hook for variant stock management
export const useVariantStock = () => {
  const { showSuccess, showError } = useNotifications();

  const updateStockMutation = useMutation({
    mutationFn: async ({ variantId, stock }: { variantId: string; stock: number }): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/variants/${variantId}/stock`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ stock }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to update variant stock');
      }
    },
    onSuccess: () => {
      showSuccess('Variant stock updated successfully');
    },
    onError: (error) => {
      showError(error.message || 'Failed to update variant stock');
    },
  });

  const bulkUpdateStockMutation = useMutation({
    mutationFn: async (updates: Array<{ variantId: string; stock: number }>): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/variants/bulk-update-stock`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ updates }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to bulk update variant stock');
      }
    },
    onSuccess: () => {
      showSuccess('Variant stock updated for all variants');
    },
    onError: (error) => {
      showError(error.message || 'Failed to update variant stock');
    },
  });

  return {
    updateStock: updateStockMutation.mutateAsync,
    bulkUpdateStock: bulkUpdateStockMutation.mutateAsync,
    isUpdating: updateStockMutation.isPending,
    isBulkUpdating: bulkUpdateStockMutation.isPending,
  };
};

// Hook for variant analytics
export const useVariantAnalytics = (productId: string) => {
  return useQuery({
    queryKey: ['variant-analytics', productId],
    queryFn: async () => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/products/${productId}/variant-analytics`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch variant analytics');
      }
      
      return response.json();
    },
    enabled: !!productId,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Utility functions for variants
export const variantUtils = {
  // Get stock status
  getStockStatus: (stock: number): 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' => {
    if (stock === 0) return 'OUT_OF_STOCK';
    if (stock <= 5) return 'LOW_STOCK';
    return 'IN_STOCK';
  },

  // Get stock status color
  getStockStatusColor: (stock: number): string => {
    const status = variantUtils.getStockStatus(stock);
    switch (status) {
      case 'IN_STOCK':
        return 'text-success-600 dark:text-success-400';
      case 'LOW_STOCK':
        return 'text-warning-600 dark:text-warning-400';
      case 'OUT_OF_STOCK':
        return 'text-error-600 dark:text-error-400';
      default:
        return 'text-secondary-600 dark:text-secondary-400';
    }
  },

  // Get stock status text
  getStockStatusText: (stock: number): string => {
    const status = variantUtils.getStockStatus(stock);
    switch (status) {
      case 'IN_STOCK':
        return `${stock} in stock`;
      case 'LOW_STOCK':
        return `Only ${stock} left`;
      case 'OUT_OF_STOCK':
        return 'Out of stock';
      default:
        return 'Stock unavailable';
    }
  },

  // Format variant combination
  formatVariantCombination: (variants: SelectedVariants): string => {
    return Object.entries(variants)
      .map(([type, value]) => `${type}: ${value}`)
      .join(', ');
  },

  // Get variant price modifier text
  getPriceModifierText: (modifier: number): string => {
    if (modifier === 0) return '';
    if (modifier > 0) return `+$${modifier.toFixed(2)}`;
    return `-$${Math.abs(modifier).toFixed(2)}`;
  },

  // Check if variant combination is valid
  isValidCombination: (variants: ProductVariant[], selectedVariants: SelectedVariants): boolean => {
    const selectedVariantObjects = Object.entries(selectedVariants).map(([type, value]) =>
      variants.find(v => v.type === type && v.value === value)
    ).filter(Boolean) as ProductVariant[];

    return selectedVariantObjects.length === Object.keys(selectedVariants).length &&
           selectedVariantObjects.every(v => v.isAvailable && v.stock > 0);
  },
};

export default useProductVariants;
