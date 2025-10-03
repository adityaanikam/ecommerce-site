import { Product, ProductCategory } from '@/types/api';

// Product Categories
export const categories: ProductCategory[] = [
  {
    name: 'Electronics',
    slug: 'electronics',
    subcategories: [
      { name: 'Mobiles', slug: 'mobiles', products: [] },
      { name: 'Headphones', slug: 'headphones', products: [] },
      { name: 'Accessories', slug: 'accessories', products: [] },
      { name: 'Chargers & Power Banks', slug: 'chargers', products: [] },
      { name: 'Tablets', slug: 'tablets', products: [] }
    ]
  },
  {
    name: 'Fashion',
    slug: 'fashion',
    subcategories: [
      { name: 'T-shirts', slug: 't-shirts', products: [] },
      { name: 'Formal Trousers', slug: 'trousers', products: [] },
      { name: 'Linen Shirts', slug: 'linen-shirts', products: [] },
      { name: 'Other', slug: 'other-fashion', products: [] }
    ]
  },
  {
    name: 'Home & Garden',
    slug: 'home-garden',
    subcategories: [
      { name: 'Curtains', slug: 'curtains', products: [] },
      { name: 'Plants', slug: 'plants', products: [] },
      { name: 'Other', slug: 'other-home', products: [] }
    ]
  },
  {
    name: 'Sports',
    slug: 'sports',
    subcategories: [
      { name: 'Gym Equipment', slug: 'gym-equipment', products: [] },
      { name: 'Gym Wear', slug: 'gym-wear', products: [] },
      { name: 'Gym Accessories', slug: 'gym-accessories', products: [] },
      { name: 'Yoga', slug: 'yoga', products: [] }
    ]
  }
];

// Product Data
export const products: Product[] = [
  // Electronics - Mobiles
  {
    id: '1',
    name: 'iPhone 15 Pro',
    brand: 'Apple',
    description: 'Experience the cutting-edge iPhone 15 Pro with its revolutionary A17 Pro chip and titanium design. Features a stunning 48MP camera system and advanced computational photography. ProMotion display delivers smooth visuals while maintaining excellent battery life.',
    price: 999.99,
    discountPrice: 949.99,
    stock: 50,
    images: [
      'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&h=800&fit=crop',
      'https://images.unsplash.com/photo-1695048132942-f9c2c5033657?w=800&h=800&fit=crop',
      'https://images.unsplash.com/photo-1695048133179-f336e6081b8f?w=800&h=800&fit=crop'
    ],
    category: 'Electronics',
    subcategory: 'Mobiles',
    specs: {
      'Screen': '6.1-inch Super Retina XDR',
      'Processor': 'A17 Pro chip',
      'Storage': '256GB',
      'Camera': '48MP Main + 12MP Ultra Wide'
    },
    features: [
      'ProMotion technology with adaptive refresh rates up to 120Hz',
      'Ceramic Shield front cover',
      'A17 Pro chip with 6-core CPU',
      'Pro camera system with 48MP Main',
      'Action mode for smooth, steady, handheld videos',
      'All-day battery life with up to 23 hours video playback'
    ],
    ratings: {
      average: 4.8,
      count: 127
    }
  },
  // Add more products following the same structure...
];

// Helper functions
export const getProductById = (id: string): Product | undefined => {
  return products.find(product => product.id === id);
};

export const getProductsByCategory = (categorySlug: string): Product[] => {
  return products.filter(product => 
    product.category.toLowerCase().replace(/\s+/g, '-') === categorySlug
  );
};

export const getProductsBySubcategory = (subcategorySlug: string): Product[] => {
  return products.filter(product => 
    product.subcategory.toLowerCase().replace(/\s+/g, '-') === subcategorySlug
  );
};

export const searchProducts = (query: string): Product[] => {
  const searchTerm = query.toLowerCase();
  return products.filter(product =>
    product.name.toLowerCase().includes(searchTerm) ||
    product.description.toLowerCase().includes(searchTerm) ||
    product.brand.toLowerCase().includes(searchTerm) ||
    product.category.toLowerCase().includes(searchTerm) ||
    product.subcategory.toLowerCase().includes(searchTerm)
  );
};

export const filterProducts = ({
  category,
  subcategory,
  minPrice,
  maxPrice,
  inStock,
  sortBy,
  sortDirection = 'asc'
}: {
  category?: string;
  subcategory?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  sortBy?: 'price' | 'name' | 'rating';
  sortDirection?: 'asc' | 'desc';
}): Product[] => {
  let filtered = [...products];

  // Apply filters
  if (category) {
    filtered = filtered.filter(p => p.category.toLowerCase().replace(/\s+/g, '-') === category);
  }
  if (subcategory) {
    filtered = filtered.filter(p => p.subcategory.toLowerCase().replace(/\s+/g, '-') === subcategory);
  }
  if (minPrice !== undefined) {
    filtered = filtered.filter(p => (p.discountPrice || p.price) >= minPrice);
  }
  if (maxPrice !== undefined) {
    filtered = filtered.filter(p => (p.discountPrice || p.price) <= maxPrice);
  }
  if (inStock) {
    filtered = filtered.filter(p => p.stock > 0);
  }

  // Apply sorting
  if (sortBy) {
    filtered.sort((a, b) => {
      let comparison = 0;
      switch (sortBy) {
        case 'price':
          comparison = (a.discountPrice || a.price) - (b.discountPrice || b.price);
          break;
        case 'name':
          comparison = a.name.localeCompare(b.name);
          break;
        case 'rating':
          comparison = (a.ratings?.average || 0) - (b.ratings?.average || 0);
          break;
      }
      return sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  return filtered;
};
