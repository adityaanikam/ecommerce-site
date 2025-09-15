// Export all services
export { apiService } from './api';
export { authService } from './authService';
export { productService } from './productService';
export { cartService } from './cartService';
export { orderService } from './orderService';

// Re-export types for convenience
export type {
  ApiResponse,
  PaginatedResponse,
  User,
  Product,
  Cart,
  Order,
  Category,
  Review,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  CreateProductRequest,
  UpdateProductRequest,
  ProductSearchRequest,
  CreateOrderRequest,
  AddToCartRequest,
} from '@/types';
