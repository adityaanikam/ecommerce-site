// Base API Response Types
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
  fieldErrors?: Record<string, string>;
  timestamp: string;
  path?: string;
  status?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  empty: boolean;
}

// User Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  imageUrl?: string;
  roles: UserRole[];
  addresses: Address[];
  provider: AuthProvider;
  providerId?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Address {
  id?: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  phoneNumber?: string;
  addressLine2?: string;
  isDefault?: boolean;
  addressType?: string;
}

export enum UserRole {
  USER = 'USER',
  SELLER = 'SELLER',
  ADMIN = 'ADMIN'
}

export enum AuthProvider {
  LOCAL = 'LOCAL',
  GOOGLE = 'GOOGLE',
  FACEBOOK = 'FACEBOOK'
}

// Product Types
export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  discountPrice?: number;
  category: string;
  subcategory?: string;
  brand: string;
  images: string[];
  specifications: Record<string, any>;
  stock: number;
  ratings: ProductRatings;
  reviews: ProductReview[];
  sellerId: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// DTO aliases used across services/hooks
// These ensure older imports like ProductDto/CategoryDto/ReviewDto resolve
export interface ProductDto extends Product {
  // Convenience, sometimes code expects flattened fields
  rating?: number; // maps to ratings.average
  reviewCount?: number; // maps to ratings.count
  sku?: string;
}

export interface ProductRatings {
  average: number;
  count: number;
  distribution: {
    5: number;
    4: number;
    3: number;
    2: number;
    1: number;
  };
}

export interface ProductReview {
  id: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
  isVerified: boolean;
  images?: string[];
}

// Category Types
export interface Category {
  id: string;
  name: string;
  description?: string;
  parentCategory?: string;
  imageUrl?: string;
  sortOrder?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export type CategoryDto = Category;

export interface CategoryTreeNode {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  sortOrder?: number;
  subcategories: CategoryTreeNode[];
}

export interface CategoryTreeResponse {
  categories: CategoryTreeNode[];
  totalCategories: number;
}

// Cart Types
export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  totalAmount: number;
  taxAmount: number;
  shippingAmount: number;
  discountAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  imageUrl?: string;
  subtotal: number;
  sku?: string;
  brand?: string;
  category?: string;
  isAvailable: boolean;
  maxQuantity?: number;
  addedAt: string;
  variant?: string;
  size?: string;
  color?: string;
  sellerId?: string;
  sellerName?: string;
}

// Order Types
export interface Order {
  id: string;
  userId: string;
  orderNumber: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  shippingAddress: Address;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  trackingNumber?: string;
  carrier?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  imageUrl?: string;
  subtotal: number;
  sku?: string;
  brand?: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  CASH_ON_DELIVERY = 'CASH_ON_DELIVERY'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  PARTIALLY_REFUNDED = 'PARTIALLY_REFUNDED'
}

// Review Types
export interface Review {
  id: string;
  productId: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
  isVerified: boolean;
  images?: string[];
  helpful?: number;
  notHelpful?: number;
}

export type ReviewDto = Review;

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  type: string;
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  imageUrl?: string;
  roles: UserRole[];
  provider: AuthProvider;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

// Search and Filter Types
export interface ProductSearchRequest {
  query?: string;
  categoryId?: string;
  subcategoryId?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  sortBy?: 'price' | 'rating' | 'name' | 'createdAt';
  sortDirection?: 'asc' | 'desc';
  inStock?: boolean;
  tags?: string[];
}

export interface SearchFilters {
  categories: string[];
  brands: string[];
  priceRange: {
    min: number;
    max: number;
  };
  ratings: number[];
  inStock: boolean;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

// Form Types
export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  discountPrice?: number;
  categoryId: string;
  subcategoryId?: string;
  brand: string;
  images?: string[];
  specifications?: Record<string, any>;
  stock: number;
  sellerId: string;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  discountPrice?: number;
  categoryId?: string;
  subcategoryId?: string;
  brand?: string;
  images?: string[];
  specifications?: Record<string, any>;
  stock?: number;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parentCategoryId?: string;
  imageUrl?: string;
  sortOrder?: number;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  parentCategoryId?: string;
  imageUrl?: string;
  sortOrder?: number;
}

export interface CreateOrderRequest {
  shippingAddress: Address;
  billingAddress?: Address;
  paymentMethod: PaymentMethod;
  notes?: string;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

// Analytics Types
export interface ProductAnalytics {
  totalViews: number;
  totalSales: number;
  conversionRate: number;
  averageRating: number;
  reviewCount: number;
  stockLevel: number;
  priceHistory: Array<{
    date: string;
    price: number;
  }>;
}

export interface OrderAnalytics {
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  ordersByStatus: Record<OrderStatus, number>;
  revenueByPeriod: Array<{
    period: string;
    revenue: number;
  }>;
  topCustomers: Array<{
    userId: string;
    totalOrders: number;
    totalSpent: number;
  }>;
}

export interface UserAnalytics {
  totalUsers: number;
  activeUsers: number;
  newUsers: number;
  usersByRole: Record<UserRole, number>;
  userGrowth: Array<{
    period: string;
    count: number;
  }>;
  topUsers: Array<{
    userId: string;
    totalOrders: number;
    totalSpent: number;
  }>;
}

// Error Types
export interface ValidationError {
  field: string;
  message: string;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: string[];
  fieldErrors?: Record<string, string>;
  timestamp: string;
  path?: string;
}
