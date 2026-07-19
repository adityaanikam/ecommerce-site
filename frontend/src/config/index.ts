// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080';

// Note: Product images are now hosted on GitHub and come as full URLs from the API
// Format: https://raw.githubusercontent.com/adityaanikam/ecommerce-site/main/backend/Products/...

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
  
  // Convert GitHub URLs to local images to avoid CORB issues
  if (path.includes('raw.githubusercontent.com')) {
    return convertGitHubUrlToLocal(path);
  }
  
  // If path is already a full URL, return as is
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // Local static assets (copied into frontend/public/images) are served from this
  // app's own origin, not the backend — must not be prefixed with IMAGE_BASE_URL.
  if (path.startsWith('/images/')) {
    return path;
  }

  // If path starts with '/', it's a relative path from the base URL
  if (path.startsWith('/')) {
    return `${IMAGE_BASE_URL}${path}`;
  }
  
  // Otherwise, treat as relative path
  return `${IMAGE_BASE_URL}/${path}`;
};

/**
 * Convert GitHub raw URL to local image path
 * @param githubUrl - The GitHub raw URL
 * @returns The local image path
 */
function convertGitHubUrlToLocal(githubUrl: string): string {
  try {
    // Extract path from GitHub URL
    // Example: https://raw.githubusercontent.com/adityaanikam/ecommerce-site/main/backend/Products/Electronics/Airpods%20Pro%202/1.webp
    // Note: some seed data incorrectly encodes spaces as "%2B" (a literal '+') instead of "%20",
    // so we normalize both before re-encoding for the actual local file request.
    const url = new URL(githubUrl);
    const pathParts = url.pathname.split('/');
    const decodePart = (part: string) => decodeURIComponent(part).replace(/\+/g, ' ');

    // Find the Products directory index
    const productsIndex = pathParts.findIndex(part => part === 'Products');
    if (productsIndex === -1) {
      console.warn('Invalid GitHub URL format:', githubUrl);
      return githubUrl;
    }

    // Extract category, product name, and image name
    const category = decodePart(pathParts[productsIndex + 1]);
    const productName = decodePart(pathParts[productsIndex + 2]);
    const imageName = decodePart(pathParts[productsIndex + 3]);

    // Construct local path, properly encoding each segment for the request
    const localPath = `/images/products/${encodeURIComponent(category)}/${encodeURIComponent(productName)}/${encodeURIComponent(imageName)}`;
    return localPath;
  } catch (error) {
    console.warn('Error converting GitHub URL to local path:', error);
    return githubUrl;
  }
}

// Product names in the database sometimes carry characters (e.g. a trailing `"` for
// inches, as in `iPad Pro 11"`) that are illegal in Windows folder names, so they can
// never match the actual asset folder on disk. Strip them before building the path.
const sanitizeForFilesystem = (segment: string): string => segment.replace(/["*:<>?|]/g, '').trim();

// Builds a same-origin local image path (files are copied into frontend/public/images/products
// to avoid CORB/CORS issues loading images cross-origin from the backend or GitHub).
const getLocalProductImagePath = (category: string, name: string, fileName: string): string => {
  return `/images/products/${encodeURIComponent(sanitizeForFilesystem(category))}/${encodeURIComponent(sanitizeForFilesystem(name))}/${fileName}`;
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
    
    return getLocalProductImagePath(category, name, `${imageIndex + 1}.${ext}`);
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
  
  return getLocalProductImagePath(category, name, `${imageIndex + 1}.${ext}`);
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
      
      urls.push(getLocalProductImagePath(category, name, `${i + 1}.${ext}`));
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
    
    urls.push(getLocalProductImagePath(category, name, `${i + 1}.${ext}`));
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
  const currentSrc = target.src;

  // Local product images are generated with a best-guess extension (jpg/webp) since the
  // live database doesn't reliably reflect what each product's actual files are. If the
  // guess was wrong, try the other extension once before giving up on a placeholder.
  if (currentSrc.includes('/images/products/') && !target.dataset.extRetried) {
    target.dataset.extRetried = 'true';
    if (currentSrc.includes('.jpg')) {
      target.src = currentSrc.replace('.jpg', '.webp');
      return;
    } else if (currentSrc.includes('.webp')) {
      target.src = currentSrc.replace('.webp', '.jpg');
      return;
    }
  }

  target.src = getFallbackImageUrl(size);
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
