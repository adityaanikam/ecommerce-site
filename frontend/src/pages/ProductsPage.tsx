import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Grid, List, Search, SlidersHorizontal } from 'lucide-react';
import { Container, Button, Pagination } from '@/components';
import { ProductCard, ProductCardSkeleton, SearchFilters } from '@/components/ecommerce';
import { useCart } from '@/contexts/CartContext';
import { useWishlist } from '@/contexts/WishlistContext';
import { productService, ProductFilters } from '@/services/productService';
import { Product } from '@/types/api';

const ITEMS_PER_PAGE = 12;

export const ProductsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { addToCart } = useCart();
  const { addToWishlist, isInWishlist } = useWishlist();
  const [viewMode, setViewMode] = React.useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = React.useState(false);
  const [isLoading, setIsLoading] = React.useState(true);
  const [products, setProducts] = React.useState<Product[]>([]);
  const [totalPages, setTotalPages] = React.useState(0);
  const [totalElements, setTotalElements] = React.useState(0);

  // URL parameters
  const page = parseInt(searchParams.get('page') || '1');
  const categorySlug = searchParams.get('category') || '';
  const subcategory = searchParams.get('subcategory') || '';
  const search = searchParams.get('search') || '';
  const minPrice = searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined;
  const maxPrice = searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined;
  const sortBy = searchParams.get('sortBy') as 'price' | 'name' | 'rating' | undefined;
  const sortOrder = searchParams.get('sortOrder') as 'asc' | 'desc' | undefined;

  // Map category slug to proper category name
  const categoryMap: Record<string, string> = {
    'electronics': 'Electronics',
    'fashion': 'Fashion', 
    'sports': 'Sports',
    'home-garden': 'Home & Garden'
  };
  const category = categorySlug ? categoryMap[categorySlug] || categorySlug : '';

  // Fetch products from backend
  React.useEffect(() => {
    const fetchProducts = async () => {
      setIsLoading(true);
      try {
        const filters: ProductFilters = {
          category: category || undefined,
          subcategory: subcategory || undefined,
          search: search || undefined,
          minPrice,
          maxPrice,
          sortBy,
          sortOrder,
          page: page - 1, // Backend uses 0-based pagination
          size: ITEMS_PER_PAGE
        };

        const response = await productService.getProducts(filters);
        setProducts(response.content);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
      } catch (error) {
        console.error('Failed to fetch products:', error instanceof Error ? error.message : String(error));
        setProducts([]);
        setTotalPages(0);
        setTotalElements(0);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProducts();
  }, [page, category, subcategory, search, minPrice, maxPrice, sortBy, sortOrder]);

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

  const handlePageChange = (newPage: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', newPage.toString());
    setSearchParams(newParams);
  };

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Filters Sidebar */}
          <div className={`lg:w-80 ${showFilters ? 'block' : 'hidden lg:block'}`}>
            <SearchFilters
              category={category}
              subcategory={subcategory}
              minPrice={minPrice}
              maxPrice={maxPrice}
              search={search}
              onCategoryChange={(cat) => updateFilters({ category: cat, subcategory: '' })}
              onSubcategoryChange={(sub) => updateFilters({ subcategory: sub })}
              onPriceChange={(min, max) => updateFilters({ minPrice: min?.toString() || '', maxPrice: max?.toString() || '' })}
              onSearchChange={(term) => updateFilters({ search: term })}
              onClearFilters={() => setSearchParams(new URLSearchParams())}
            />
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
                        {totalElements} products found
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
                  </Button>

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
            </div>

            {/* Products Grid/List */}
            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {Array.from({ length: ITEMS_PER_PAGE }).map((_, i) => (
                  <ProductCardSkeleton key={i} />
                ))}
              </div>
                ) : products.length > 0 ? (
              <div className={
                viewMode === 'grid'
                  ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6'
                  : 'space-y-4'
              }>
                    {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    variant={viewMode === 'list' ? 'compact' : 'default'}
                        onAddToCart={(id, quantity) => {
                          const product = products.find(p => p.id === id);
                          if (product) addToCart(product, quantity);
                        }}
                        onAddToWishlist={(id) => {
                          const product = products.find(p => p.id === id);
                          if (product) addToWishlist(product);
                        }}
                        isInWishlist={(id) => isInWishlist(id)}
                        onQuickView={(id) => navigate(`/products/${id}`)}
                        onViewDetails={(id) => navigate(`/products/${id}`)}
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
                <Button onClick={() => setSearchParams(new URLSearchParams())}>
                  Clear All Filters
                </Button>
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-8 flex justify-center">
                <Pagination
                  currentPage={page}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </div>
        </div>
      </Container>
    </div>
  );
};

export default ProductsPage;