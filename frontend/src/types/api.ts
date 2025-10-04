// Base API Response Types
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: string[];
  fieldErrors?: Record<string, string>;
  timestamp?: string;
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

// Product Types
export interface Product {
  id: string;
  name: string;
  brand: string;
  description: string;
  price: number;
  discountPrice?: number;
  stock: number;
  images: string[];
  category: string;
  subcategory: string;
  specs: Record<string, string>;
  features?: string[];
  ratings?: {
    average: number;
    count: number;
  };
  rating?: number; // For backward compatibility
  sku?: string;
  slug?: string;
  isFeatured?: boolean;
  isNew?: boolean;
  isOnSale?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductCategory {
  name: string;
  slug: string;
  subcategories: ProductSubcategory[];
}

export interface ProductSubcategory {
  name: string;
  slug: string;
  products: Product[];
}

// Cart Types
export interface CartItem {
  productId: string;
  quantity: number;
  name: string;
  price: number;
  image: string;
  stock: number;
}

export interface Cart {
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}

// Order Types
export interface Order {
  id: string;
  userId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: Address;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  productId: string;
  name: string;
  quantity: number;
  price: number;
  image: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

// Address Types
export interface Address {
  id?: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  isDefault?: boolean;
}

// Search and Filter Types
export interface ProductSearchRequest {
  query?: string;
  category?: string;
  subcategory?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: 'price' | 'name' | 'newest';
  sortDirection?: 'asc' | 'desc';
  inStock?: boolean;
  page?: number;
  pageSize?: number;
}

export interface SearchFilters {
  categories: string[];
  subcategories: string[];
  brands: string[];
  priceRange: {
    min: number;
    max: number;
  };
  inStock: boolean;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

// DTO Types (Data Transfer Objects)
export interface UserDto {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  avatar?: string;
  phone?: string;
  address?: Address;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDto extends Product {}
export interface CategoryDto extends ProductCategory {}
export interface CartDto extends Cart {}
export interface CartItemDto extends CartItem {}
export interface OrderDto extends Order {}
export interface OrderItemDto extends OrderItem {}

export interface ReviewDto {
  id: string;
  productId: string;
  userId: string;
  rating: number;
  title: string;
  comment: string;
  helpful: number;
  createdAt: string;
  updatedAt: string;
}

export interface AuthDto {
  user: UserDto;
  token: string;
  refreshToken: string;
  expiresIn: number;
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