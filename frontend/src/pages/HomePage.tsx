import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { Container, Button } from '@/components';
import { CategoryCard, ProductCard } from '@/components/ecommerce';
import { useQuery } from '@tanstack/react-query';
import { categoryService } from '@/services/categoryService';
import { productService } from '@/services/productService';
import { useCart } from '@/contexts/CartContext';
import { useWishlist } from '@/contexts/WishlistContext';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  
  const { addToCart } = useCart();
  const { addToWishlist, isInWishlist } = useWishlist();

  const { data: categories, isLoading: categoriesLoading, error: categoriesError, refetch: refetchCategories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getCategories(),
  });

  const { data: featuredProducts, isLoading: productsLoading, error: productsError, refetch: refetchProducts } = useQuery({
    queryKey: ['featured-products'],
    queryFn: () => productService.getProducts({
      page: 0,
      size: 8,
      sortBy: 'rating',
      sortOrder: 'desc'
    }),
    select: (data) => data.content,
  });

  const isLoading = categoriesLoading || productsLoading;
  const error = categoriesError || productsError;
  const refetch = () => {
    refetchCategories();
    refetchProducts();
  };

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary-600 to-primary-800">
        <div className="absolute inset-0 bg-grid-white/10" />
        <Container className="relative py-24 lg:py-32">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-8 animate-fadeIn">
              <div className="space-y-5">
                <h1 className="text-4xl lg:text-6xl font-bold text-white tracking-tight leading-tight">
                  Discover Amazing
                  <span className="block text-yellow-300 mt-1 drop-shadow-md">Products</span>
                </h1>
                <p className="text-xl text-primary-50 max-w-lg leading-relaxed">
                  Shop the latest trends in electronics, fashion, home decor, and more.
                  Quality products at unbeatable prices with fast, free shipping.
                </p>
              </div>

              <div className="flex flex-col sm:flex-row gap-4">
                <Button
                  size="lg"
                  variant="secondary"
                  className="w-full sm:w-auto shadow-xl hover:shadow-yellow-400/20 bg-yellow-400 hover:bg-yellow-500 text-primary-900"
                  onClick={() => navigate('/products')}
                >
                  Shop Now
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="w-full sm:w-auto bg-white/10 border-white border-opacity-50 text-white hover:bg-white hover:text-primary-600 hover:border-white backdrop-blur-sm shadow-lg hover:shadow-xl transition-all duration-300"
                  onClick={() => navigate('/categories')}
                >
                  Browse Categories
                </Button>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-3 gap-8 pt-8">
                <div className="bg-white/10 backdrop-blur-sm text-center py-3 px-2 rounded-xl">
                  <div className="text-3xl font-bold text-white">10K+</div>
                  <div className="text-primary-200 font-medium text-sm">Happy Customers</div>
                </div>
                <div className="bg-white/10 backdrop-blur-sm text-center py-3 px-2 rounded-xl">
                  <div className="text-3xl font-bold text-white">150+</div>
                  <div className="text-primary-200 font-medium text-sm">Products</div>
                </div>
                <div className="bg-white/10 backdrop-blur-sm text-center py-3 px-2 rounded-xl">
                  <div className="text-3xl font-bold text-white">99%</div>
                  <div className="text-primary-200 font-medium text-sm">Satisfaction</div>
                </div>
              </div>
            </div>

            <div className="relative animate-float">
              <div className="aspect-square bg-white/10 rounded-2xl backdrop-blur-lg border border-white/20 p-5 shadow-2xl">
                <img
                  src="https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=800&h=800"
                  alt="Featured Products"
                  className="w-full h-full object-cover rounded-xl shadow-md"
                  style={{ boxShadow: '0 0 30px rgba(255,255,255,0.1)' }}
                />
              </div>

              {/* Floating elements */}
              <div className="absolute -top-6 -right-6 bg-yellow-400 text-primary-900 px-6 py-3 rounded-full font-bold shadow-xl transform hover:scale-105 transition-transform duration-300 border-2 border-yellow-300">
                Up to 50% OFF
              </div>
            </div>
          </div>
        </Container>
      </section>

      {/* Categories Section */}
      <section className="py-16">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Shop by Category
            </h2>
            <p className="text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
              Explore our wide range of products across different categories.
              From the latest electronics to trendy fashion, we've got everything you need.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {isLoading ? (
              // Loading skeleton
              Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="aspect-[4/3] bg-secondary-200 dark:bg-secondary-700 rounded-xl mb-4" />
                  <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded w-3/4 mb-2" />
                  <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded w-1/2" />
                </div>
              ))
            ) : error ? (
              <div className="col-span-4 text-center py-12">
                <p className="text-secondary-600 dark:text-secondary-400">
                  Failed to load categories. Please try again later.
                </p>
                <Button onClick={() => refetch()} className="mt-4">
                  Retry
                </Button>
              </div>
            ) : categories ? (
              categories.map((category) => (
                <CategoryCard
                  key={category.slug}
                  name={category.name}
                  description={category.description}
                  image={category.image}
                  productCount={category.subcategories.reduce((acc, sub) => acc + sub.count, 0)}
                  onClick={() => navigate(`/products?category=${category.slug}`)}
                />
              ))
            ) : null}
          </div>
        </Container>
      </section>

      {/* Featured Products Section */}
      <section className="py-16 bg-white dark:bg-secondary-800">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Featured Products
            </h2>
            <p className="text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
              Check out our most popular products and latest arrivals.
              Don't miss out on these amazing deals!
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {isLoading ? (
              // Loading skeleton
              Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="aspect-square bg-secondary-200 dark:bg-secondary-700 rounded-xl mb-4" />
                  <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded w-3/4 mb-2" />
                  <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded w-1/2" />
                </div>
              ))
            ) : error ? (
              <div className="col-span-4 text-center py-12">
                <p className="text-secondary-600 dark:text-secondary-400">
                  Failed to load featured products. Please try again later.
                </p>
                <Button onClick={() => refetch()} className="mt-4">
                  Retry
                </Button>
              </div>
            ) : featuredProducts ? (
              featuredProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  onAddToCart={(id, quantity) => {
                    const product = featuredProducts.find(p => p.id === id);
                    if (product) addToCart(product, quantity);
                  }}
                  onAddToWishlist={(id) => {
                    const product = featuredProducts.find(p => p.id === id);
                    if (product) addToWishlist(product);
                  }}
                  isInWishlist={(id) => isInWishlist(id)}
                  onQuickView={(id) => navigate(`/products/${id}`)}
                  onViewDetails={(id) => navigate(`/products/${id}`)}
                />
              ))
            ) : null}
          </div>

          {/* View All Button */}
          <div className="text-center mt-12">
            <Button
              size="lg"
              onClick={() => navigate('/products')}
              className="bg-primary-600 hover:bg-primary-700 text-white shadow-md hover:shadow-lg"
            >
              View All Products
              <ArrowRight className="ml-2 h-5 w-5" />
            </Button>
          </div>
        </Container>
      </section>
    </div>
  );
};

export default HomePage;