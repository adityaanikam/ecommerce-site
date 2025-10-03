// API Configuration
export const API_BASE_URL = 'http://localhost:8080/api';

// Feature Flags
export const FEATURES = {
  ENABLE_DARK_MODE: true,
  ENABLE_CART: true,
  ENABLE_WISHLIST: true,
  ENABLE_SEARCH: true,
};

// Pagination
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 12,
  DEFAULT_PAGE: 0,
};

// Image Configuration
export const IMAGES = {
  PLACEHOLDER_URL: 'https://placehold.co/800x800/6366f1/ffffff',
  FALLBACK_URL: '/placeholder.jpg',
};

// Local Storage Keys
export const STORAGE_KEYS = {
  CART: 'ecommerce-cart',
  WISHLIST: 'ecommerce-wishlist',
  THEME: 'ecommerce-theme',
  AUTH: 'ecommerce-auth',
};

// Route Paths
export const ROUTES = {
  HOME: '/',
  PRODUCTS: '/products',
  PRODUCT_DETAILS: '/products/:id',
  CATEGORIES: '/categories',
  DEALS: '/deals',
  CART: '/cart',
  CHECKOUT: '/checkout',
  WISHLIST: '/wishlist',
  NOT_FOUND: '/404',
};

// Category Images
export const CATEGORY_IMAGES = {
  'Electronics': 'https://images.unsplash.com/photo-1510557880182-3d4d3c3f0633?w=800',
  'Fashion': 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=800',
  'Home & Garden': 'https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=800',
  'Sports': 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800',
};

// Category Descriptions
export const CATEGORY_DESCRIPTIONS = {
  'Electronics': 'Latest mobiles, headphones, tablets, accessories and chargers.',
  'Fashion': 'T-shirts, formal, casual, linen and accessories for all.',
  'Home & Garden': 'Curtains, plants and furniture to style your home.',
  'Sports': 'Gym equipment, wear and accessories to stay fit.',
};
