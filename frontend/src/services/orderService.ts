import { apiService } from './api';
import {
  Order,
  CreateOrderRequest,
  OrderStatus,
  PaymentStatus,
  PaginatedResponse,
  ApiResponse,
} from '@/types';

export const orderService = {
  // Create order
  createOrder: async (orderData: CreateOrderRequest): Promise<Order> => {
    const response = await apiService.post<Order>('/orders', orderData);
    return response.data!;
  },

  // Get order by ID
  getOrder: async (id: string): Promise<Order> => {
    const response = await apiService.get<Order>(`/orders/${id}`);
    return response.data!;
  },

  // Get order by order number
  getOrderByNumber: async (orderNumber: string): Promise<Order> => {
    const response = await apiService.get<Order>(`/orders/order-number/${orderNumber}`);
    return response.data!;
  },

  // Get user's orders
  getUserOrders: async (page = 0, size = 20): Promise<PaginatedResponse<Order>> => {
    const response = await apiService.get<PaginatedResponse<Order>>(
      `/orders/my-orders?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get order history
  getOrderHistory: async (): Promise<Order[]> => {
    const response = await apiService.get<Order[]>('/orders/history');
    return response.data!;
  },

  // Update order status (Admin/Seller)
  updateOrderStatus: async (id: string, status: OrderStatus): Promise<Order> => {
    const response = await apiService.put<Order>(`/orders/${id}/status?status=${status}`);
    return response.data!;
  },

  // Update payment status (Admin)
  updatePaymentStatus: async (id: string, paymentStatus: PaymentStatus): Promise<Order> => {
    const response = await apiService.put<Order>(`/orders/${id}/payment-status?paymentStatus=${paymentStatus}`);
    return response.data!;
  },

  // Cancel order
  cancelOrder: async (id: string, reason?: string): Promise<Order> => {
    const response = await apiService.post<Order>(
      `/orders/${id}/cancel${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`
    );
    return response.data!;
  },

  // Add tracking info (Admin/Seller)
  addTrackingInfo: async (id: string, trackingNumber: string, carrier: string): Promise<Order> => {
    const response = await apiService.post<Order>(
      `/orders/${id}/tracking?trackingNumber=${encodeURIComponent(trackingNumber)}&carrier=${encodeURIComponent(carrier)}`
    );
    return response.data!;
  },

  // Get tracking info
  getTrackingInfo: async (id: string): Promise<{
    orderNumber: string;
    trackingNumber: string;
    carrier: string;
    status: OrderStatus;
    shippingAddress: any;
  }> => {
    const response = await apiService.get<{
      orderNumber: string;
      trackingNumber: string;
      carrier: string;
      status: OrderStatus;
      shippingAddress: any;
    }>(`/orders/${id}/tracking`);
    return response.data!;
  },

  // Get all orders (Admin)
  getAllOrders: async (page = 0, size = 20): Promise<PaginatedResponse<Order>> => {
    const response = await apiService.get<PaginatedResponse<Order>>(
      `/orders?page=${page}&size=${size}`
    );
    return response.data!;
  },

  // Get orders by status (Admin)
  getOrdersByStatus: async (status: OrderStatus): Promise<Order[]> => {
    const response = await apiService.get<Order[]>(`/orders/by-status?status=${status}`);
    return response.data!;
  },

  // Get orders by payment status (Admin)
  getOrdersByPaymentStatus: async (paymentStatus: PaymentStatus): Promise<Order[]> => {
    const response = await apiService.get<Order[]>(`/orders/by-payment-status?paymentStatus=${paymentStatus}`);
    return response.data!;
  },

  // Get orders by date range (Admin)
  getOrdersByDateRange: async (startDate: string, endDate: string): Promise<Order[]> => {
    const response = await apiService.get<Order[]>(
      `/orders/by-date-range?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
    );
    return response.data!;
  },

  // Get order analytics (Admin)
  getOrderAnalytics: async (): Promise<{
    totalOrders: number;
    totalRevenue: number;
    averageOrderValue: number;
    ordersByStatus: Record<OrderStatus, number>;
    revenueByPeriod: Array<{ period: string; revenue: number }>;
    topCustomers: Array<{ userId: string; totalOrders: number; totalSpent: number }>;
  }> => {
    const response = await apiService.get<{
      totalOrders: number;
      totalRevenue: number;
      averageOrderValue: number;
      ordersByStatus: Record<OrderStatus, number>;
      revenueByPeriod: Array<{ period: string; revenue: number }>;
      topCustomers: Array<{ userId: string; totalOrders: number; totalSpent: number }>;
    }>('/orders/analytics/summary');
    return response.data!;
  },

  // Get revenue analytics (Admin)
  getRevenueAnalytics: async (startDate?: string, endDate?: string): Promise<{
    totalRevenue: number;
    revenueByPeriod: Record<string, number>;
    revenueByStatus: Record<PaymentStatus, number>;
  }> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    
    const response = await apiService.get<{
      totalRevenue: number;
      revenueByPeriod: Record<string, number>;
      revenueByStatus: Record<PaymentStatus, number>;
    }>(`/orders/analytics/revenue?${params.toString()}`);
    return response.data!;
  },

  // Get user order analytics (Admin)
  getUserOrderAnalytics: async (userId: string): Promise<{
    userId: string;
    totalOrderValue: number;
    orderCount: number;
    averageOrderValue: number;
  }> => {
    const response = await apiService.get<{
      userId: string;
      totalOrderValue: number;
      orderCount: number;
      averageOrderValue: number;
    }>(`/orders/analytics/user/${userId}`);
    return response.data!;
  },
};
