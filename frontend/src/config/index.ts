// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080';

// Production configuration
export const IS_PRODUCTION = import.meta.env.PROD;
export const APP_NAME = import.meta.env.VITE_APP_NAME || 'E-commerce Store';
export const APP_DESCRIPTION = import.meta.env.VITE_APP_DESCRIPTION || 'Modern e-commerce platform';

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

/**
 * Get the full image URL by combining base URL with image path
 * @param path - The image path (can be relative or absolute)
 * @returns The full image URL
 */
export const getImageUrl = (path: string): string => {
  if (!path) {
    return `${IMAGE_BASE_URL}/placeholder.jpg`;
  }
  
  // If path is already a full URL, return as is
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // If path starts with '/', it's a relative path from the base URL
  if (path.startsWith('/')) {
    return `${IMAGE_BASE_URL}${path}`;
  }
  
  // Otherwise, treat as relative path
  return `${IMAGE_BASE_URL}/${path}`;
};

// Special function to generate correct image paths for products
export const getProductImageUrl = (product: any, imageIndex: number = 0): string => {
  const category = product.category || 'Electronics';
  const name = product.name || 'Unknown';
  
  // For Sports category, always generate correct paths with proper extensions
  if (category === 'Sports') {
    let ext = 'jpg'; // Default to jpg
    
    // Sports products that use webp (most of them)
    if (name.includes('Ab Wheel') || name.includes('Adjustable Dumbbells') || name.includes('Athletic Shirt') ||
        name.includes('Balance Ball') || name.includes('Compression Leggings') || name.includes('Foam Roller') ||
        name.includes('Gym Bag') || name.includes('Gym Shorts') || name.includes('Gym Towel') ||
        name.includes('Kettlebell') || name.includes('Lifting Gloves') || name.includes('Massage Ball') ||
        name.includes('Meditation Cushion') || name.includes('Pull-up Bar') || name.includes('Recovery Bands') ||
        name.includes('Sports Bra') || name.includes('Tank Top') || name.includes('Water Bottle') ||
        name.includes('Yoga Blocks') || name.includes('Yoga Mat') || name.includes('Yoga Strap')) {
      ext = 'webp';
    }
    
    return getImageUrl(`/products/${category}/${name}/${imageIndex + 1}.${ext}`);
  }
  
  // For other categories, use database images if available and not placeholders
  if (product.images && product.images.length > 0 && !product.images[0].includes('placehold.co')) {
    return getImageUrl(product.images[imageIndex] || product.images[0]);
  }
  
  // Generate correct path based on product info for other categories
  let ext = 'jpg'; // Default to jpg
  
  // Check for products that use webp
  if (name.includes('AirPods') || name.includes('iPhone 13') || name.includes('iPhone 14') || 
      name.includes('Bluetooth Speaker') || name.includes('MagSafe') || name.includes('OnePlus') ||
      name.includes('Oppo') || name.includes('Poco') || name.includes('RAVPower') ||
      name.includes('Black Crew Neck') || name.includes('Black Jeans') || name.includes('Blue Dress Shirt') ||
      name.includes('Canvas Belt') || name.includes('Cargo Shorts') || name.includes('Checkered Shirt') ||
      name.includes('Compression Shirt') || name.includes('Dress Pants') || name.includes('Gray Tee') ||
      name.includes('Khaki Chinos') || name.includes('Leather Belt') || name.includes('Linen Blazer') ||
      name.includes('Linen Pants') || name.includes('Linen Shorts') || name.includes('Navy Tee') ||
      name.includes('Oxford Shirt') || name.includes('Regular Fit Jeans') || name.includes('Running Shorts') ||
      name.includes('Slim Fit Jeans') || name.includes('Striped Linen Shirt') || name.includes('Striped Tee') ||
      name.includes('Track Pants') || name.includes('White Dress Shirt') || name.includes('White Linen Shirt') ||
      name.includes('Yoga Pants')) {
    ext = 'webp';
  }
  
  return getImageUrl(`/products/${category}/${name}/${imageIndex + 1}.${ext}`);
};

// Function to get all image URLs for a product
export const getProductImageUrls = (product: any): string[] => {
  const category = product.category || 'Electronics';
  const name = product.name || 'Unknown';
  
  // For Sports category, always generate correct paths with proper extensions
  if (category === 'Sports') {
    const urls = [];
    for (let i = 0; i < 3; i++) {
      let ext = 'jpg'; // Default to jpg
      
      // Sports products that use webp (most of them)
      if (name.includes('Ab Wheel') || name.includes('Adjustable Dumbbells') || name.includes('Athletic Shirt') ||
          name.includes('Balance Ball') || name.includes('Compression Leggings') || name.includes('Foam Roller') ||
          name.includes('Gym Bag') || name.includes('Gym Shorts') || name.includes('Gym Towel') ||
          name.includes('Kettlebell') || name.includes('Lifting Gloves') || name.includes('Massage Ball') ||
          name.includes('Meditation Cushion') || name.includes('Pull-up Bar') || name.includes('Recovery Bands') ||
          name.includes('Sports Bra') || name.includes('Tank Top') || name.includes('Water Bottle') ||
          name.includes('Yoga Blocks') || name.includes('Yoga Mat') || name.includes('Yoga Strap')) {
        ext = 'webp';
      }
      
      urls.push(getImageUrl(`/products/${category}/${name}/${i + 1}.${ext}`));
    }
    return urls;
  }
  
  // For other categories, use database images if available and not placeholders
  if (product.images && product.images.length > 0 && !product.images[0].includes('placehold.co')) {
    return product.images.map((img: string) => getImageUrl(img));
  }
  
  // Generate correct paths based on product info for other categories
  const urls = [];
  for (let i = 0; i < 3; i++) {
    let ext = 'jpg'; // Default to jpg
    
    // Check for products that use webp
    if (name.includes('AirPods') || name.includes('iPhone 13') || name.includes('iPhone 14') || 
        name.includes('Bluetooth Speaker') || name.includes('MagSafe') || name.includes('OnePlus') ||
        name.includes('Oppo') || name.includes('Poco') || name.includes('RAVPower') ||
        name.includes('Black Crew Neck') || name.includes('Black Jeans') || name.includes('Blue Dress Shirt') ||
        name.includes('Canvas Belt') || name.includes('Cargo Shorts') || name.includes('Checkered Shirt') ||
        name.includes('Compression Shirt') || name.includes('Dress Pants') || name.includes('Gray Tee') ||
        name.includes('Khaki Chinos') || name.includes('Leather Belt') || name.includes('Linen Blazer') ||
        name.includes('Linen Pants') || name.includes('Linen Shorts') || name.includes('Navy Tee') ||
        name.includes('Oxford Shirt') || name.includes('Regular Fit Jeans') || name.includes('Running Shorts') ||
        name.includes('Slim Fit Jeans') || name.includes('Striped Linen Shirt') || name.includes('Striped Tee') ||
        name.includes('Track Pants') || name.includes('White Dress Shirt') || name.includes('White Linen Shirt') ||
        name.includes('Yoga Pants')) {
      ext = 'webp';
    }
    
    urls.push(getImageUrl(`/products/${category}/${name}/${i + 1}.${ext}`));
  }
  
  return urls;
};

/**
 * Get fallback image URL for error handling
 * @param size - The size of the placeholder image (default: 800x800)
 * @returns The fallback image URL
 */
export const getFallbackImageUrl = (size: string = '800x800'): string => {
  return `https://placehold.co/${size}/6366f1/ffffff?text=Image+Not+Found`;
};

/**
 * Handle image load error by setting a fallback image
 * @param event - The error event
 * @param size - The size of the fallback image
 */
export const handleImageError = (event: React.SyntheticEvent<HTMLImageElement, Event>, size: string = '800x800') => {
  const target = event.currentTarget;
  // Try alternative extensions for Sports products
  const currentSrc = target.src;
  if (currentSrc.includes('/Sports/')) {
    // Try switching between jpg and webp
    if (currentSrc.includes('.jpg')) {
      target.src = currentSrc.replace('.jpg', '.webp');
    } else if (currentSrc.includes('.webp')) {
      target.src = currentSrc.replace('.webp', '.jpg');
    } else {
      target.src = getFallbackImageUrl(size);
    }
  } else {
    target.src = getFallbackImageUrl(size);
  }
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
