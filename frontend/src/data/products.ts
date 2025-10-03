import { Product } from '@/types/api';

export const products: Product[] = [
  // Electronics (50 products)
  {
    id: 'ELEC001',
    name: 'iPhone 15 Pro',
    brand: 'Apple',
    price: 999,
    category: 'Electronics',
    subcategory: 'Mobiles',
    description: 'Latest iPhone with titanium design and A17 Pro chip',
    stock: 50,
    images: ['/api/placeholder/300/300'],
    rating: 4.5,
    reviewCount: 10,
    specs: {
      'Display': '6.1-inch Super Retina XDR',
      'Processor': 'A17 Pro chip',
      'Camera': '48MP Main + 12MP Ultra Wide',
      'Battery': 'Up to 23 hours video playback'
    },
    features: [
      'Titanium design',
      'Action button',
      'USB-C connector',
      'ProMotion technology'
    ]
  },
  {
    id: 'ELEC002',
    name: 'Samsung Galaxy S24',
    brand: 'Samsung',
    price: 899,
    category: 'Electronics',
    subcategory: 'Mobiles',
    description: 'Premium Android phone with AI features',
    stock: 45,
    images: ['/api/placeholder/300/300'],
    rating: 4.4,
    reviewCount: 8,
    specs: {
      'Display': '6.2-inch Dynamic AMOLED 2X',
      'Processor': 'Snapdragon 8 Gen 3',
      'Camera': '50MP Main + 12MP Ultra Wide',
      'Battery': '4,000 mAh'
    },
    features: [
      'AI-powered features',
      'Samsung Knox security',
      'Wireless PowerShare',
      'IP68 water resistance'
    ]
  },
  // Add remaining electronics products...

  // Fashion (50 products)
  {
    id: 'FASH001',
    name: 'Cotton Crew Neck Tee',
    brand: 'BasicWear',
    price: 19,
    category: 'Fashion',
    subcategory: 'T-shirts',
    description: 'Comfortable cotton t-shirt for everyday wear',
    stock: 100,
    images: ['/api/placeholder/300/300'],
    rating: 4.3,
    reviewCount: 15,
    specs: {
      'Material': '100% Cotton',
      'Fit': 'Regular fit',
      'Care': 'Machine washable',
      'Size': 'S to XXL'
    },
    features: [
      'Breathable fabric',
      'Ribbed crew neck',
      'Double-stitched hem',
      'Pre-shrunk cotton'
    ]
  },
  // Add remaining fashion products...

  // Home & Garden (25 products)
  {
    id: 'HOME001',
    name: 'Blackout Curtains',
    brand: 'HomeStyle',
    price: 45,
    category: 'Home & Garden',
    subcategory: 'Curtains',
    description: 'Room darkening curtains for better sleep',
    stock: 30,
    images: ['/api/placeholder/300/300'],
    rating: 4.6,
    reviewCount: 12,
    specs: {
      'Material': 'Polyester',
      'Size': '52" x 84"',
      'Package': '2 panels',
      'Care': 'Machine washable'
    },
    features: [
      '99% light blocking',
      'Thermal insulation',
      'Noise reduction',
      'Easy installation'
    ]
  },
  // Add remaining home products...

  // Sports (25 products)
  {
    id: 'SPORT001',
    name: 'Adjustable Dumbbells',
    brand: 'FitPro',
    price: 149,
    category: 'Sports',
    subcategory: 'Gym Equipment',
    description: 'Space-saving adjustable weight dumbbells',
    stock: 25,
    images: ['/api/placeholder/300/300'],
    rating: 4.7,
    reviewCount: 20,
    specs: {
      'Weight Range': '5-52.5 lbs',
      'Material': 'Steel plates',
      'Handle': 'Ergonomic grip',
      'Adjustment': '2.5 lb increments'
    },
    features: [
      'Quick weight adjustment',
      'Compact storage',
      'Durable construction',
      'Safety lock mechanism'
    ]
  },
  // Add remaining sports products...
];

export const getProductById = (id: string): Product | undefined => {
  return products.find(product => product.id === id);
};

export const getProductsByCategory = (category: string): Product[] => {
  return products.filter(product => product.category.toLowerCase() === category.toLowerCase());
};

export const searchProducts = (query: string): Product[] => {
  const searchTerm = query.toLowerCase();
  return products.filter(product => 
    product.name.toLowerCase().includes(searchTerm) ||
    product.description.toLowerCase().includes(searchTerm) ||
    product.brand.toLowerCase().includes(searchTerm)
  );
};

export type FilterOptions = {
  category?: string;
  subcategory?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  sortBy?: 'price' | 'name' | 'rating';
  sortOrder?: 'asc' | 'desc';
};

export const filterProducts = (list: Product[], options: FilterOptions): Product[] => {
  const {
    category,
    subcategory,
    minPrice,
    maxPrice,
    search,
    sortBy,
    sortOrder = 'asc',
  } = options;

  let result = [...list];

  if (category) {
    result = result.filter(p => p.category && p.category.toLowerCase() === category.toLowerCase());
  }
  if (subcategory) {
    result = result.filter(p => p.subcategory && p.subcategory.toLowerCase() === subcategory.toLowerCase());
  }
  if (minPrice !== undefined) {
    result = result.filter(p => p.price >= minPrice);
  }
  if (maxPrice !== undefined) {
    result = result.filter(p => p.price <= maxPrice);
  }
  if (search) {
    const term = search.toLowerCase();
    result = result.filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.brand.toLowerCase().includes(term) ||
      p.description.toLowerCase().includes(term)
    );
  }

  if (sortBy) {
    result.sort((a: any, b: any) => {
      let va = a[sortBy];
      let vb = b[sortBy];
      if (sortBy === 'name') {
        va = String(va).toLowerCase();
        vb = String(vb).toLowerCase();
        return sortOrder === 'asc' ? va.localeCompare(vb) : vb.localeCompare(va);
      }
      return sortOrder === 'asc' ? (va ?? 0) - (vb ?? 0) : (vb ?? 0) - (va ?? 0);
    });
  }

  return result;
};

// Derived categories structure for UI (HomePage, SearchFilters)
const slugify = (value: string) =>
  value.toLowerCase().replace(/&/g, 'and').replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');

export const categories = (() => {
  const map = new Map<string, Map<string, number>>();
  for (const p of products) {
    if (!map.has(p.category)) map.set(p.category, new Map());
    const subMap = map.get(p.category)!;
    subMap.set(p.subcategory, (subMap.get(p.subcategory) || 0) + 1);
  }

  const imageByCategory: Record<string, string> = {
    'Electronics': 'https://images.unsplash.com/photo-1510557880182-3d4d3c3f0633?w=800',
    'Fashion': 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=800',
    'Home & Garden': 'https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=800',
    'Sports': 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800',
  };
  const descByCategory: Record<string, string> = {
    'Electronics': 'Latest mobiles, headphones, tablets, accessories and chargers.',
    'Fashion': 'T-shirts, formal, casual, linen and accessories for all.',
    'Home & Garden': 'Curtains, plants and furniture to style your home.',
    'Sports': 'Gym equipment, wear and accessories to stay fit.',
  };

  return Array.from(map.entries()).map(([categoryName, subMap]) => ({
    name: categoryName,
    slug: slugify(categoryName),
    description: descByCategory[categoryName] || '',
    image: imageByCategory[categoryName] || 'https://via.placeholder.com/800x600/6366f1/ffffff?text=Product',
    subcategories: Array.from(subMap.entries()).map(([subName, count]) => ({
      name: subName,
      slug: slugify(subName),
      count,
    })),
  }));
})();