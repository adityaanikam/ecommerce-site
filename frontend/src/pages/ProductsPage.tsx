import React from 'react';
import { useSearchParams } from 'react-router-dom';
import { 
  Grid, 
  List, 
  Filter, 
  Search, 
  SortAsc, 
  SortDesc, 
  ChevronLeft, 
  ChevronRight,
  X,
  SlidersHorizontal
} from 'lucide-react';
import { Container, Grid as GridLayout, Button, Input, Dropdown, Badge, Skeleton } from '@/components';
import { ProductCard } from '@/components/ecommerce';
import { useDebounce } from '@/hooks';

// Mock data - in real app, this would come from API
const mockProducts = Array.from({ length: 24 }, (_, i) => ({
  id: `${i + 1}`,
  name: `Product ${i + 1}`,
  brand: ['TechSound', 'FitTech', 'PowerUp', 'SoundWave'][i % 4],
  price: Math.floor(Math.random() * 500) + 50,
  discountPrice: Math.random() > 0.5 ? Math.floor(Math.random() * 400) + 30 : undefined,
  images: [`/api/placeholder/300/300?text=Product+${i + 1}`],
  ratings: { 
    average: Math.round((Math.random() * 2 + 3) * 10) / 10, 
    count: Math.floor(Math.random() * 200) + 10 
  },
  stock: Math.floor(Math.random() * 50) + 1,
  description: `High-quality product ${i + 1} with excellent features and performance.`,
  category: ['Electronics', 'Fashion', 'Home & Garden', 'Sports'][i % 4],
  tags: ['New', 'Sale', 'Popular', 'Featured'][Math.floor(Math.random() * 4)]
}));

const sortOptions = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'price-asc', label: 'Price: Low to High' },
  { value: 'price-desc', label: 'Price: High to Low' },
  { value: 'rating', label: 'Customer Rating' },
  { value: 'newest', label: 'Newest First' },
  { value: 'popular', label: 'Most Popular' }
];

const filterCategories = [
  { value: 'electronics', label: 'Electronics', count: 245 },
  { value: 'fashion', label: 'Fashion', count: 189 },
  { value: 'home-garden', label: 'Home & Garden', count: 156 },
  { value: 'sports', label: 'Sports', count: 98 }
];

const priceRanges = [
  { value: '0-50', label: 'Under $50' },
  { value: '50-100', label: '$50 - $100' },
  { value: '100-200', label: '$100 - $200' },
  { value: '200-500', label: '$200 - $500' },
  { value: '500+', label: 'Over $500' }
];

const brands = [
  { value: 'techsound', label: 'TechSound', count: 45 },
  { value: 'fittech', label: 'FitTech', count: 32 },
  { value: 'powerup', label: 'PowerUp', count: 28 },
  { value: 'soundwave', label: 'SoundWave', count: 41 }
];

export const ProductsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [viewMode, setViewMode] = React.useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = React.useState(false);
  const [isLoading, setIsLoading] = React.useState(false);

  // URL parameters
  const search = searchParams.get('search') || '';
  const category = searchParams.get('category') || '';
  const sort = searchParams.get('sort') || 'relevance';
  const page = parseInt(searchParams.get('page') || '1');
  const priceRange = searchParams.get('price') || '';
  const brand = searchParams.get('brand') || '';

  // Local state for filters
  const [localSearch, setLocalSearch] = React.useState(search);
  const [selectedCategories, setSelectedCategories] = React.useState<string[]>(
    category ? category.split(',') : []
  );
  const [selectedPriceRange, setSelectedPriceRange] = React.useState(priceRange);
  const [selectedBrands, setSelectedBrands] = React.useState<string[]>(
    brand ? brand.split(',') : []
  );

  // Debounced search
  const debouncedSearch = useDebounce(localSearch, 500);

  // Update URL when search changes
  React.useEffect(() => {
    if (debouncedSearch !== search) {
      const newParams = new URLSearchParams(searchParams);
      if (debouncedSearch) {
        newParams.set('search', debouncedSearch);
      } else {
        newParams.delete('search');
      }
      newParams.delete('page'); // Reset to first page
      setSearchParams(newParams);
    }
  }, [debouncedSearch, search, searchParams, setSearchParams]);

  // Filter and sort products
  const filteredProducts = React.useMemo(() => {
    let filtered = [...mockProducts];

    // Search filter
    if (search) {
      filtered = filtered.filter(product =>
        product.name.toLowerCase().includes(search.toLowerCase()) ||
        product.brand.toLowerCase().includes(search.toLowerCase()) ||
        product.description.toLowerCase().includes(search.toLowerCase())
      );
    }

    // Category filter
    if (selectedCategories.length > 0) {
      filtered = filtered.filter(product =>
        selectedCategories.some(cat => 
          product.category.toLowerCase().replace(/\s+/g, '-') === cat
        )
      );
    }

    // Price range filter
    if (selectedPriceRange) {
      const [min, max] = selectedPriceRange.split('-').map(Number);
      filtered = filtered.filter(product => {
        const price = product.discountPrice || product.price;
        if (max) {
          return price >= min && price <= max;
        } else {
          return price >= min;
        }
      });
    }

    // Brand filter
    if (selectedBrands.length > 0) {
      filtered = filtered.filter(product =>
        selectedBrands.some(b => 
          product.brand.toLowerCase().replace(/\s+/g, '') === b
        )
      );
    }

    // Sort products
    switch (sort) {
      case 'price-asc':
        filtered.sort((a, b) => (a.discountPrice || a.price) - (b.discountPrice || b.price));
        break;
      case 'price-desc':
        filtered.sort((a, b) => (b.discountPrice || b.price) - (a.discountPrice || a.price));
        break;
      case 'rating':
        filtered.sort((a, b) => b.ratings.average - a.ratings.average);
        break;
      case 'newest':
        filtered.sort((a, b) => parseInt(b.id) - parseInt(a.id));
        break;
      case 'popular':
        filtered.sort((a, b) => b.ratings.count - a.ratings.count);
        break;
      default:
        // Keep original order for relevance
        break;
    }

    return filtered;
  }, [search, selectedCategories, selectedPriceRange, selectedBrands, sort]);

  // Pagination
  const itemsPerPage = 12;
  const totalPages = Math.ceil(filteredProducts.length / itemsPerPage);
  const startIndex = (page - 1) * itemsPerPage;
  const paginatedProducts = filteredProducts.slice(startIndex, startIndex + itemsPerPage);

  const handleCategoryToggle = (categoryValue: string) => {
    const newCategories = selectedCategories.includes(categoryValue)
      ? selectedCategories.filter(c => c !== categoryValue)
      : [...selectedCategories, categoryValue];
    setSelectedCategories(newCategories);
    updateFilters({ category: newCategories.join(',') });
  };

  const handleBrandToggle = (brandValue: string) => {
    const newBrands = selectedBrands.includes(brandValue)
      ? selectedBrands.filter(b => b !== brandValue)
      : [...selectedBrands, brandValue];
    setSelectedBrands(newBrands);
    updateFilters({ brand: newBrands.join(',') });
  };

  const handlePriceRangeChange = (priceValue: string) => {
    setSelectedPriceRange(priceValue);
    updateFilters({ price: priceValue });
  };

  const updateFilters = (updates: Record<string, string>) => {
    const newParams = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([key, value]) => {
      if (value) {
        newParams.set(key, value);
      } else {
        newParams.delete(key);
      }
    });
    newParams.delete('page'); // Reset to first page
    setSearchParams(newParams);
  };

  const clearAllFilters = () => {
    setSelectedCategories([]);
    setSelectedPriceRange('');
    setSelectedBrands([]);
    setSearchParams(new URLSearchParams());
  };

  const activeFiltersCount = selectedCategories.length + 
    (selectedPriceRange ? 1 : 0) + 
    selectedBrands.length;

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Filters Sidebar */}
          <div className={`lg:w-80 ${showFilters ? 'block' : 'hidden lg:block'}`}>
            <div className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-6 sticky top-8">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                  Filters
                </h3>
                {activeFiltersCount > 0 && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearAllFilters}
                    className="text-primary-600 hover:text-primary-700"
                  >
                    Clear All
                  </Button>
                )}
              </div>

              {/* Search */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-2">
                  Search Products
                </label>
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                  <Input
                    value={localSearch}
                    onChange={(e) => setLocalSearch(e.target.value)}
                    placeholder="Search products..."
                    className="pl-10"
                  />
                </div>
              </div>

              {/* Categories */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
                  Categories
                </h4>
                <div className="space-y-2">
                  {filterCategories.map((cat) => (
                    <label key={cat.value} className="flex items-center justify-between cursor-pointer">
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={selectedCategories.includes(cat.value)}
                          onChange={() => handleCategoryToggle(cat.value)}
                          className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-secondary-300 rounded"
                        />
                        <span className="ml-2 text-sm text-secondary-700 dark:text-secondary-300">
                          {cat.label}
                        </span>
                      </div>
                      <Badge variant="secondary" className="text-xs">
                        {cat.count}
                      </Badge>
                    </label>
                  ))}
                </div>
              </div>

              {/* Price Range */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
                  Price Range
                </h4>
                <div className="space-y-2">
                  {priceRanges.map((range) => (
                    <label key={range.value} className="flex items-center cursor-pointer">
                      <input
                        type="radio"
                        name="priceRange"
                        value={range.value}
                        checked={selectedPriceRange === range.value}
                        onChange={(e) => handlePriceRangeChange(e.target.value)}
                        className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-secondary-300"
                      />
                      <span className="ml-2 text-sm text-secondary-700 dark:text-secondary-300">
                        {range.label}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Brands */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
                  Brands
                </h4>
                <div className="space-y-2">
                  {brands.map((brand) => (
                    <label key={brand.value} className="flex items-center justify-between cursor-pointer">
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={selectedBrands.includes(brand.value)}
                          onChange={() => handleBrandToggle(brand.value)}
                          className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-secondary-300 rounded"
                        />
                        <span className="ml-2 text-sm text-secondary-700 dark:text-secondary-300">
                          {brand.label}
                        </span>
                      </div>
                      <Badge variant="secondary" className="text-xs">
                        {brand.count}
                      </Badge>
                    </label>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Main Content */}
          <div className="flex-1">
            {/* Header */}
            <div className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-6 mb-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h1 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100">
                    Products
                  </h1>
                  <p className="text-secondary-600 dark:text-secondary-400">
                    {filteredProducts.length} products found
                  </p>
                </div>

                <div className="flex items-center gap-4">
                  {/* Mobile Filter Toggle */}
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setShowFilters(!showFilters)}
                    className="lg:hidden"
                  >
                    <SlidersHorizontal className="h-4 w-4 mr-2" />
                    Filters
                    {activeFiltersCount > 0 && (
                      <Badge variant="error" className="ml-2">
                        {activeFiltersCount}
                      </Badge>
                    )}
                  </Button>

                  {/* Sort */}
                  <Dropdown
                    options={sortOptions}
                    value={sort}
                    onChange={(value) => updateFilters({ sort: value })}
                    placeholder="Sort by"
                    size="sm"
                  />

                  {/* View Mode */}
                  <div className="flex border border-secondary-300 dark:border-secondary-600 rounded-lg">
                    <Button
                      variant={viewMode === 'grid' ? 'default' : 'ghost'}
                      size="sm"
                      onClick={() => setViewMode('grid')}
                      className="rounded-r-none"
                    >
                      <Grid className="h-4 w-4" />
                    </Button>
                    <Button
                      variant={viewMode === 'list' ? 'default' : 'ghost'}
                      size="sm"
                      onClick={() => setViewMode('list')}
                      className="rounded-l-none"
                    >
                      <List className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>

              {/* Active Filters */}
              {activeFiltersCount > 0 && (
                <div className="flex flex-wrap gap-2 mt-4">
                  {selectedCategories.map((cat) => (
                    <Badge key={cat} variant="secondary" className="flex items-center gap-1">
                      {filterCategories.find(c => c.value === cat)?.label}
                      <button
                        onClick={() => handleCategoryToggle(cat)}
                        className="ml-1 hover:text-error-600"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                  {selectedPriceRange && (
                    <Badge variant="secondary" className="flex items-center gap-1">
                      {priceRanges.find(r => r.value === selectedPriceRange)?.label}
                      <button
                        onClick={() => handlePriceRangeChange('')}
                        className="ml-1 hover:text-error-600"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  )}
                  {selectedBrands.map((brand) => (
                    <Badge key={brand} variant="secondary" className="flex items-center gap-1">
                      {brands.find(b => b.value === brand)?.label}
                      <button
                        onClick={() => handleBrandToggle(brand)}
                        className="ml-1 hover:text-error-600"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                </div>
              )}
            </div>

            {/* Products Grid/List */}
            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {Array.from({ length: 8 }).map((_, i) => (
                  <SkeletonCard key={i} />
                ))}
              </div>
            ) : paginatedProducts.length > 0 ? (
              <div className={
                viewMode === 'grid'
                  ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
                  : 'space-y-4'
              }>
                {paginatedProducts.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    variant={viewMode === 'list' ? 'compact' : 'default'}
                    onAddToCart={(id) => console.log('Add to cart:', id)}
                    onAddToWishlist={(id) => console.log('Add to wishlist:', id)}
                    onQuickView={(id) => console.log('Quick view:', id)}
                  />
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <div className="text-secondary-400 mb-4">
                  <Search className="h-16 w-16 mx-auto" />
                </div>
                <h3 className="text-lg font-medium text-secondary-900 dark:text-secondary-100 mb-2">
                  No products found
                </h3>
                <p className="text-secondary-600 dark:text-secondary-400 mb-4">
                  Try adjusting your search or filter criteria
                </p>
                <Button onClick={clearAllFilters}>
                  Clear All Filters
                </Button>
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mt-12">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => updateFilters({ page: (page - 1).toString() })}
                  disabled={page === 1}
                >
                  <ChevronLeft className="h-4 w-4" />
                  Previous
                </Button>

                <div className="flex items-center gap-1">
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const pageNum = i + 1;
                    return (
                      <Button
                        key={pageNum}
                        variant={page === pageNum ? 'default' : 'outline'}
                        size="sm"
                        onClick={() => updateFilters({ page: pageNum.toString() })}
                        className="w-10"
                      >
                        {pageNum}
                      </Button>
                    );
                  })}
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => updateFilters({ page: (page + 1).toString() })}
                  disabled={page === totalPages}
                >
                  Next
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            )}
          </div>
        </div>
      </Container>
    </div>
  );
};