import { apiClient, ApiResponse } from '@/lib/axios';
import { OrderDto, OrderItemDto } from '@/types/api';

export interface CreateOrderRequest {
  shippingAddress: {
    firstName: string;
    lastName: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
    phoneNumber: string;
  };
  billingAddress?: {
    firstName: string;
    lastName: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
    phoneNumber: string;
  };
  paymentMethod: {
    type: 'CARD' | 'PAYPAL' | 'APPLE_PAY' | 'GOOGLE_PAY';
    cardDetails?: {
      number: string;
      expiryDate: string;
      cvv: string;
      cardholderName: string;
    };
    billingAddress?: {
      firstName: string;
      lastName: string;
      address: string;
      city: string;
      state: string;
      zipCode: string;
      country: string;
    };
  };
  shippingMethod: string;
  couponCode?: string;
  notes?: string;
}

export interface UpdateOrderRequest {
  id: string;
  status?: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';
  trackingNumber?: string;
  notes?: string;
}

export interface OrderFilters {
  status?: string;
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
}

export interface OrderListResponse {
  orders: OrderDto[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export interface OrderAnalytics {
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  ordersByStatus: Record<string, number>;
  ordersByMonth: Array<{ month: string; count: number; revenue: number }>;
  topProducts: Array<{ productId: string; productName: string; quantity: number; revenue: number }>;
}

export interface OrderTrackingInfo {
  orderId: string;
  status: string;
  trackingNumber?: string;
  estimatedDelivery?: string;
  trackingHistory: Array<{
    status: string;
    timestamp: string;
    location?: string;
    description: string;
  }>;
  carrier?: {
    name: string;
    trackingUrl: string;
  };
}

export class OrderService {
  private static readonly BASE_PATH = '/orders';

  // Create new order
  static async createOrder(data: CreateOrderRequest): Promise<ApiResponse<OrderDto>> {
    const response = await apiClient.post(this.BASE_PATH, data);
    return response.data;
  }

  // Get user's orders
  static async getUserOrders(params: {
    page: number;
    limit: number;
    filters?: OrderFilters;
  }): Promise<ApiResponse<OrderListResponse>> {
    const response = await apiClient.get(`${this.BASE_PATH}/user`, { params });
    return response.data;
  }

  // Get order by ID
  static async getOrderById(id: string): Promise<ApiResponse<OrderDto>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  // Update order (admin only)
  static async updateOrder(data: UpdateOrderRequest): Promise<ApiResponse<OrderDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/${data.id}`, data);
    return response.data;
  }

  // Cancel order
  static async cancelOrder(id: string, reason?: string): Promise<ApiResponse<OrderDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${id}/cancel`, { reason });
    return response.data;
  }

  // Request order return
  static async requestReturn(orderId: string, data: {
    items: Array<{
      orderItemId: string;
      quantity: number;
      reason: string;
    }>;
    reason: string;
    notes?: string;
  }): Promise<ApiResponse<{ returnId: string; status: string }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${orderId}/return`, data);
    return response.data;
  }

  // Get order tracking information
  static async getOrderTracking(orderId: string): Promise<ApiResponse<OrderTrackingInfo>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${orderId}/tracking`);
    return response.data;
  }

  // Get order invoice
  static async getOrderInvoice(orderId: string): Promise<Blob> {
    const response = await apiClient.get(`${this.BASE_PATH}/${orderId}/invoice`, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Download order invoice
  static async downloadOrderInvoice(orderId: string): Promise<void> {
    const blob = await this.getOrderInvoice(orderId);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `invoice-${orderId}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  // Get order analytics (admin only)
  static async getOrderAnalytics(params?: {
    dateFrom?: string;
    dateTo?: string;
    groupBy?: 'day' | 'week' | 'month';
  }): Promise<ApiResponse<OrderAnalytics>> {
    const response = await apiClient.get(`${this.BASE_PATH}/analytics`, { params });
    return response.data;
  }

  // Get all orders (admin only)
  static async getAllOrders(params: {
    page: number;
    limit: number;
    filters?: OrderFilters;
    sortBy?: string;
    sortOrder?: 'asc' | 'desc';
  }): Promise<ApiResponse<OrderListResponse>> {
    const response = await apiClient.get(this.BASE_PATH, { params });
    return response.data;
  }

  // Process payment for order
  static async processPayment(orderId: string, paymentData: {
    paymentMethodId: string;
    amount: number;
    currency: string;
  }): Promise<ApiResponse<{
    paymentId: string;
    status: string;
    transactionId?: string;
  }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${orderId}/payment`, paymentData);
    return response.data;
  }

  // Refund order
  static async refundOrder(orderId: string, data: {
    amount?: number;
    reason: string;
    refundToOriginalPayment?: boolean;
  }): Promise<ApiResponse<{
    refundId: string;
    status: string;
    amount: number;
  }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${orderId}/refund`, data);
    return response.data;
  }

  // Get order status history
  static async getOrderStatusHistory(orderId: string): Promise<ApiResponse<Array<{
    status: string;
    timestamp: string;
    notes?: string;
    updatedBy?: string;
  }>>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${orderId}/status-history`);
    return response.data;
  }

  // Add order note (admin only)
  static async addOrderNote(orderId: string, note: string): Promise<ApiResponse<void>> {
    const response = await apiClient.post(`${this.BASE_PATH}/${orderId}/notes`, { note });
    return response.data;
  }

  // Get order notes
  static async getOrderNotes(orderId: string): Promise<ApiResponse<Array<{
    id: string;
    note: string;
    createdAt: string;
    createdBy: string;
  }>>> {
    const response = await apiClient.get(`${this.BASE_PATH}/${orderId}/notes`);
    return response.data;
  }

  // Bulk update orders (admin only)
  static async bulkUpdateOrders(updates: Array<{
    orderId: string;
    status?: string;
    trackingNumber?: string;
  }>): Promise<ApiResponse<{ updated: number; failed: number }>> {
    const response = await apiClient.put(`${this.BASE_PATH}/bulk-update`, { updates });
    return response.data;
  }

  // Export orders (admin only)
  static async exportOrders(params: {
    format: 'csv' | 'excel';
    filters?: OrderFilters;
    dateFrom?: string;
    dateTo?: string;
  }): Promise<Blob> {
    const response = await apiClient.get(`${this.BASE_PATH}/export`, {
      params,
      responseType: 'blob'
    });
    return response.data;
  }

  // Get order statistics
  static async getOrderStatistics(): Promise<ApiResponse<{
    totalOrders: number;
    pendingOrders: number;
    completedOrders: number;
    cancelledOrders: number;
    totalRevenue: number;
    averageOrderValue: number;
    ordersThisMonth: number;
    revenueThisMonth: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/statistics`);
    return response.data;
  }
}
