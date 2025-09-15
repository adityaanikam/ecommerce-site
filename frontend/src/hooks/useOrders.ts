import { useQuery, useMutation, useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { OrderService, CreateOrderRequest, OrderFilters } from '@/services/api';
import { OrderDto } from '@/types/api';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';
import { useNotifications } from '@/contexts/NotificationContext';

// Hook for getting user orders with pagination
export const useUserOrders = (params: { page: number; limit: number; filters?: OrderFilters }) => {
  return useQuery({
    queryKey: queryKeys.orders.list(params.filters || {}),
    queryFn: async () => {
      const response = await OrderService.getUserOrders(params);
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
    keepPreviousData: true,
  });
};

// Hook for infinite scrolling orders
export const useInfiniteUserOrders = (filters?: OrderFilters) => {
  return useInfiniteQuery({
    queryKey: queryKeys.orders.list(filters || {}),
    queryFn: async ({ pageParam = 1 }) => {
      const response = await OrderService.getUserOrders({
        page: pageParam,
        limit: 10,
        filters,
      });
      return response.data;
    },
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for getting a single order
export const useOrder = (id: string) => {
  return useQuery({
    queryKey: queryKeys.orders.detail(id),
    queryFn: async () => {
      const response = await OrderService.getOrderById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for creating an order
export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: OrderService.createOrder,
    onSuccess: (response) => {
      // Invalidate orders list
      invalidateQueries.orders();
      
      // Clear cart after successful order
      queryClient.setQueryData(queryKeys.cart.current, null);
      
      showSuccess(`Order #${response.data.orderNumber} created successfully!`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for canceling an order
export const useCancelOrder = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) => 
      OrderService.cancelOrder(id, reason),
    onSuccess: (response, variables) => {
      // Invalidate specific order and orders list
      invalidateQueries.order(variables.id);
      invalidateQueries.orders();
      
      showSuccess(`Order #${response.data.orderNumber} cancelled successfully`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for requesting order return
export const useRequestReturn = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ({ orderId, data }: { orderId: string; data: any }) => 
      OrderService.requestReturn(orderId, data),
    onSuccess: (response, variables) => {
      // Invalidate specific order and orders list
      invalidateQueries.order(variables.orderId);
      invalidateQueries.orders();
      
      showSuccess('Return request submitted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting order tracking information
export const useOrderTracking = (orderId: string) => {
  return useQuery({
    queryKey: queryKeys.orders.tracking(orderId),
    queryFn: async () => {
      const response = await OrderService.getOrderTracking(orderId);
      return response.data;
    },
    enabled: !!orderId,
    staleTime: 1 * 60 * 1000, // 1 minute (tracking updates frequently)
    refetchInterval: 5 * 60 * 1000, // Refetch every 5 minutes
  });
};

// Hook for downloading order invoice
export const useDownloadInvoice = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: OrderService.downloadOrderInvoice,
    onSuccess: () => {
      showSuccess('Invoice downloaded successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for processing payment
export const useProcessPayment = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ({ orderId, paymentData }: { orderId: string; paymentData: any }) => 
      OrderService.processPayment(orderId, paymentData),
    onSuccess: (response, variables) => {
      // Invalidate specific order and orders list
      invalidateQueries.order(variables.orderId);
      invalidateQueries.orders();
      
      showSuccess('Payment processed successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for refunding an order
export const useRefundOrder = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ({ orderId, data }: { orderId: string; data: any }) => 
      OrderService.refundOrder(orderId, data),
    onSuccess: (response, variables) => {
      // Invalidate specific order and orders list
      invalidateQueries.order(variables.orderId);
      invalidateQueries.orders();
      
      showSuccess(`Refund of $${response.data.amount} processed successfully`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting order status history
export const useOrderStatusHistory = (orderId: string) => {
  return useQuery({
    queryKey: ['orders', orderId, 'status-history'],
    queryFn: async () => {
      const response = await OrderService.getOrderStatusHistory(orderId);
      return response.data;
    },
    enabled: !!orderId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for adding order note (admin only)
export const useAddOrderNote = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: ({ orderId, note }: { orderId: string; note: string }) => 
      OrderService.addOrderNote(orderId, note),
    onSuccess: (response, variables) => {
      // Invalidate order notes
      queryClient.invalidateQueries({
        queryKey: ['orders', variables.orderId, 'notes'],
      });
      
      showSuccess('Note added successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting order notes
export const useOrderNotes = (orderId: string) => {
  return useQuery({
    queryKey: ['orders', orderId, 'notes'],
    queryFn: async () => {
      const response = await OrderService.getOrderNotes(orderId);
      return response.data;
    },
    enabled: !!orderId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for getting order analytics (admin only)
export const useOrderAnalytics = (params?: { dateFrom?: string; dateTo?: string; groupBy?: 'day' | 'week' | 'month' }) => {
  return useQuery({
    queryKey: queryKeys.orders.analytics,
    queryFn: async () => {
      const response = await OrderService.getOrderAnalytics(params);
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for getting all orders (admin only)
export const useAllOrders = (params: {
  page: number;
  limit: number;
  filters?: OrderFilters;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}) => {
  return useQuery({
    queryKey: ['orders', 'admin', params],
    queryFn: async () => {
      const response = await OrderService.getAllOrders(params);
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
    keepPreviousData: true,
  });
};

// Hook for updating order (admin only)
export const useUpdateOrder = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: OrderService.updateOrder,
    onSuccess: (response, variables) => {
      // Invalidate specific order and orders list
      invalidateQueries.order(variables.id);
      invalidateQueries.orders();
      
      showSuccess('Order updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for bulk updating orders (admin only)
export const useBulkUpdateOrders = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: OrderService.bulkUpdateOrders,
    onSuccess: (response) => {
      // Invalidate all orders
      invalidateQueries.orders();
      
      showSuccess(`${response.data.updated} orders updated successfully`);
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for exporting orders (admin only)
export const useExportOrders = () => {
  const { showSuccess, showError } = useNotifications();

  return useMutation({
    mutationFn: OrderService.exportOrders,
    onSuccess: () => {
      showSuccess('Orders exported successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });
};

// Hook for getting order statistics
export const useOrderStatistics = () => {
  return useQuery({
    queryKey: queryKeys.orders.statistics,
    queryFn: async () => {
      const response = await OrderService.getOrderStatistics();
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
