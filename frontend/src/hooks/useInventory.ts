import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useWebSocket } from './useWebSocket';
import { useNotifications } from '@/contexts/NotificationContext';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';

interface InventoryUpdate {
  productId: string;
  stock: number;
  reservedStock: number;
  availableStock: number;
  lastUpdated: string;
}

interface InventoryAlert {
  productId: string;
  productName: string;
  currentStock: number;
  threshold: number;
  alertType: 'LOW_STOCK' | 'OUT_OF_STOCK' | 'RESTOCKED';
}

// Hook for real-time inventory updates
export const useInventory = (productId?: string) => {
  const queryClient = useQueryClient();
  const { showWarning, showInfo } = useNotifications();

  // WebSocket connection for real-time updates
  const { isConnected, sendMessage } = useWebSocket({
    url: `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}/inventory${productId ? `?productId=${productId}` : ''}`,
    onMessage: (message) => {
      if (message.type === 'INVENTORY_UPDATE') {
        const update: InventoryUpdate = message.data;
        
        // Update product cache
        queryClient.setQueryData(
          queryKeys.products.detail(update.productId),
          (oldData: any) => {
            if (!oldData) return oldData;
            return {
              ...oldData,
              stock: update.stock,
              availableStock: update.availableStock,
            };
          }
        );

        // Invalidate product lists to reflect changes
        invalidateQueries.products();

        // Show notification for significant changes
        if (update.availableStock === 0) {
          showWarning(`Product is now out of stock`);
        } else if (update.availableStock <= 5) {
          showWarning(`Only ${update.availableStock} items left in stock`);
        }
      } else if (message.type === 'INVENTORY_ALERT') {
        const alert: InventoryAlert = message.data;
        
        switch (alert.alertType) {
          case 'LOW_STOCK':
            showWarning(`${alert.productName} is running low on stock (${alert.currentStock} remaining)`);
            break;
          case 'OUT_OF_STOCK':
            showWarning(`${alert.productName} is now out of stock`);
            break;
          case 'RESTOCKED':
            showInfo(`${alert.productName} has been restocked (${alert.currentStock} available)`);
            break;
        }
      }
    },
  });

  // Get inventory status for a product
  const getInventoryStatus = (stock: number): 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' => {
    if (stock === 0) return 'OUT_OF_STOCK';
    if (stock <= 5) return 'LOW_STOCK';
    return 'IN_STOCK';
  };

  // Get stock status color
  const getStockStatusColor = (stock: number): string => {
    const status = getInventoryStatus(stock);
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
  };

  // Get stock status text
  const getStockStatusText = (stock: number): string => {
    const status = getInventoryStatus(stock);
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
  };

  // Check if product is available for purchase
  const isProductAvailable = (stock: number): boolean => {
    return stock > 0;
  };

  // Get estimated delivery time based on stock
  const getEstimatedDelivery = (stock: number): string => {
    if (stock === 0) return 'Currently unavailable';
    if (stock <= 5) return '2-3 business days';
    return '1-2 business days';
  };

  return {
    isConnected,
    getInventoryStatus,
    getStockStatusColor,
    getStockStatusText,
    isProductAvailable,
    getEstimatedDelivery,
  };
};

// Hook for inventory alerts (admin)
export const useInventoryAlerts = () => {
  return useQuery({
    queryKey: ['inventory', 'alerts'],
    queryFn: async () => {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/inventory/alerts`);
      if (!response.ok) throw new Error('Failed to fetch inventory alerts');
      return response.json();
    },
    refetchInterval: 30000, // Refetch every 30 seconds
    staleTime: 10000, // Consider data stale after 10 seconds
  });
};

// Hook for low stock products
export const useLowStockProducts = () => {
  return useQuery({
    queryKey: ['inventory', 'low-stock'],
    queryFn: async () => {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/inventory/low-stock`);
      if (!response.ok) throw new Error('Failed to fetch low stock products');
      return response.json();
    },
    refetchInterval: 60000, // Refetch every minute
  });
};

// Hook for inventory management (admin)
export const useInventoryManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  const updateStockMutation = useMutation({
    mutationFn: async ({ productId, newStock }: { productId: string; newStock: number }) => {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/inventory/${productId}/stock`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ stock: newStock }),
      });
      if (!response.ok) throw new Error('Failed to update stock');
      return response.json();
    },
    onSuccess: () => {
      invalidateQueries.products();
      showSuccess('Stock updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  const bulkUpdateStockMutation = useMutation({
    mutationFn: async (updates: Array<{ productId: string; stock: number }>) => {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/admin/inventory/bulk-update`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ updates }),
      });
      if (!response.ok) throw new Error('Failed to bulk update stock');
      return response.json();
    },
    onSuccess: () => {
      invalidateQueries.products();
      showSuccess('Stock updated for all products');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    updateStock: updateStockMutation.mutateAsync,
    bulkUpdateStock: bulkUpdateStockMutation.mutateAsync,
    isUpdating: updateStockMutation.isPending,
    isBulkUpdating: bulkUpdateStockMutation.isPending,
  };
};

export default useInventory;
