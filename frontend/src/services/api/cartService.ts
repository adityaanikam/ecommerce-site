import { apiClient, ApiResponse } from '@/lib/axios';
import { CartDto, CartItemDto } from '@/types/api';

export interface AddToCartRequest {
  productId: string;
  quantity: number;
  variant?: {
    size?: string;
    color?: string;
    style?: string;
  };
}

export interface UpdateCartItemRequest {
  itemId: string;
  quantity: number;
}

export interface RemoveFromCartRequest {
  itemId: string;
}

export interface MoveToWishlistRequest {
  itemId: string;
}

export interface CartSummary {
  totalItems: number;
  totalPrice: number;
  totalDiscount: number;
  shippingCost: number;
  tax: number;
  finalTotal: number;
  freeShippingThreshold: number;
  isEligibleForFreeShipping: boolean;
}

export interface CartValidationResult {
  isValid: boolean;
  errors: Array<{
    itemId: string;
    productId: string;
    message: string;
    type: 'OUT_OF_STOCK' | 'PRICE_CHANGED' | 'PRODUCT_UNAVAILABLE' | 'INVALID_QUANTITY';
  }>;
  warnings: Array<{
    itemId: string;
    message: string;
  }>;
}

export class CartService {
  private static readonly BASE_PATH = '/cart';

  // Get current user's cart
  static async getCart(): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.get(this.BASE_PATH);
    return response.data;
  }

  // Add item to cart
  static async addToCart(data: AddToCartRequest): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/items`, data);
    return response.data;
  }

  // Update cart item quantity
  static async updateCartItem(data: UpdateCartItemRequest): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/items/${data.itemId}`, {
      quantity: data.quantity
    });
    return response.data;
  }

  // Remove item from cart
  static async removeFromCart(itemId: string): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/items/${itemId}`);
    return response.data;
  }

  // Clear entire cart
  static async clearCart(): Promise<ApiResponse<void>> {
    const response = await apiClient.delete(this.BASE_PATH);
    return response.data;
  }

  // Move item to wishlist
  static async moveToWishlist(itemId: string): Promise<ApiResponse<{ cart: CartDto; wishlistItem: any }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/items/${itemId}/move-to-wishlist`);
    return response.data;
  }

  // Get cart summary
  static async getCartSummary(): Promise<ApiResponse<CartSummary>> {
    const response = await apiClient.get(`${this.BASE_PATH}/summary`);
    return response.data;
  }

  // Validate cart
  static async validateCart(): Promise<ApiResponse<CartValidationResult>> {
    const response = await apiClient.get(`${this.BASE_PATH}/validate`);
    return response.data;
  }

  // Apply coupon code
  static async applyCoupon(couponCode: string): Promise<ApiResponse<{
    cart: CartDto;
    discount: number;
    message: string;
  }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/coupon`, { couponCode });
    return response.data;
  }

  // Remove coupon code
  static async removeCoupon(): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.delete(`${this.BASE_PATH}/coupon`);
    return response.data;
  }

  // Get shipping options
  static async getShippingOptions(): Promise<ApiResponse<Array<{
    id: string;
    name: string;
    description: string;
    cost: number;
    estimatedDays: string;
    isFree: boolean;
  }>>> {
    const response = await apiClient.get(`${this.BASE_PATH}/shipping-options`);
    return response.data;
  }

  // Set shipping option
  static async setShippingOption(shippingOptionId: string): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/shipping`, { shippingOptionId });
    return response.data;
  }

  // Get cart abandonment data (for analytics)
  static async getCartAbandonmentData(): Promise<ApiResponse<{
    abandonedCarts: number;
    recoveryRate: number;
    averageAbandonmentTime: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/abandonment-data`);
    return response.data;
  }

  // Save cart for later (guest users)
  static async saveCartForLater(cartData: CartDto): Promise<ApiResponse<{ cartId: string }>> {
    const response = await apiClient.post(`${this.BASE_PATH}/save`, cartData);
    return response.data;
  }

  // Restore saved cart
  static async restoreCart(cartId: string): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/restore`, { cartId });
    return response.data;
  }

  // Merge guest cart with user cart (after login)
  static async mergeGuestCart(guestCartId: string): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.post(`${this.BASE_PATH}/merge`, { guestCartId });
    return response.data;
  }

  // Get cart recommendations
  static async getCartRecommendations(): Promise<ApiResponse<Array<{
    productId: string;
    reason: string;
    confidence: number;
  }>>> {
    const response = await apiClient.get(`${this.BASE_PATH}/recommendations`);
    return response.data;
  }

  // Bulk update cart items
  static async bulkUpdateCartItems(updates: Array<{
    itemId: string;
    quantity?: number;
    variant?: any;
  }>): Promise<ApiResponse<CartDto>> {
    const response = await apiClient.put(`${this.BASE_PATH}/bulk-update`, { updates });
    return response.data;
  }

  // Get cart history (for returning customers)
  static async getCartHistory(params: {
    page: number;
    limit: number;
  }): Promise<ApiResponse<{
    carts: Array<CartDto & { createdAt: string; status: string }>;
    totalCount: number;
  }>> {
    const response = await apiClient.get(`${this.BASE_PATH}/history`, { params });
    return response.data;
  }
}
