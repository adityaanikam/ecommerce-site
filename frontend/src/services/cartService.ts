import { apiService } from './api';
import {
  Cart,
  CartItem,
  AddToCartRequest,
  ApiResponse,
} from '@/types';

export const cartService = {
  // Get user's cart
  getCart: async (): Promise<Cart> => {
    const response = await apiService.get<Cart>('/cart');
    return response.data!;
  },

  // Add item to cart
  addItem: async (productId: string, quantity: number): Promise<Cart> => {
    const response = await apiService.post<Cart>('/cart/items', {
      productId,
      quantity,
    });
    return response.data!;
  },

  // Update item quantity
  updateItemQuantity: async (productId: string, quantity: number): Promise<Cart> => {
    const response = await apiService.put<Cart>(`/cart/items/${productId}?quantity=${quantity}`);
    return response.data!;
  },

  // Remove item from cart
  removeItem: async (productId: string): Promise<Cart> => {
    const response = await apiService.delete<Cart>(`/cart/items/${productId}`);
    return response.data!;
  },

  // Clear cart
  clearCart: async (): Promise<Cart> => {
    const response = await apiService.delete<Cart>('/cart');
    return response.data!;
  },

  // Validate cart
  validateCart: async (): Promise<Cart> => {
    const response = await apiService.post<Cart>('/cart/validate');
    return response.data!;
  },

  // Save cart
  saveCart: async (cart: Cart): Promise<void> => {
    await apiService.post('/cart/save', cart);
  },

  // Apply coupon
  applyCoupon: async (couponCode: string): Promise<Cart> => {
    const response = await apiService.post<Cart>(`/cart/coupon?couponCode=${encodeURIComponent(couponCode)}`);
    return response.data!;
  },

  // Remove coupon
  removeCoupon: async (): Promise<Cart> => {
    const response = await apiService.delete<Cart>('/cart/coupon');
    return response.data!;
  },

  // Get cart summary
  getCartSummary: async (): Promise<{
    itemCount: number;
    totalItems: number;
    subtotal: number;
    taxAmount: number;
    shippingAmount: number;
    discountAmount: number;
    totalAmount: number;
  }> => {
    const response = await apiService.get<{
      itemCount: number;
      totalItems: number;
      subtotal: number;
      taxAmount: number;
      shippingAmount: number;
      discountAmount: number;
      totalAmount: number;
    }>('/cart/summary');
    return response.data!;
  },

  // Get cart item count
  getCartItemCount: async (): Promise<{
    itemCount: number;
    isEmpty: boolean;
  }> => {
    const response = await apiService.get<{
      itemCount: number;
      isEmpty: boolean;
    }>('/cart/item-count');
    return response.data!;
  },

  // Get abandoned carts (Admin)
  getAbandonedCarts: async (daysSinceLastUpdate = 7): Promise<Cart[]> => {
    const response = await apiService.get<Cart[]>(`/cart/abandoned?daysSinceLastUpdate=${daysSinceLastUpdate}`);
    return response.data!;
  },

  // Cleanup expired carts (Admin)
  cleanupExpiredCarts: async (daysSinceLastUpdate = 30): Promise<void> => {
    await apiService.post(`/cart/cleanup?daysSinceLastUpdate=${daysSinceLastUpdate}`);
  },
};
