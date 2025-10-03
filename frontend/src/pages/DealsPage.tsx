import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button } from '@/components';
import { ProductCard, ProductCardSkeleton } from '@/components/ecommerce';
import { useCart } from '@/contexts/CartContext';
import { useWishlist } from '@/contexts/WishlistContext';
import { productService } from '@/services/productService';
import { Product } from '@/types/api';

export const DealsPage: React.FC = () => {
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { addToWishlist, isInWishlist } = useWishlist();
  const [isLoading, setIsLoading] = React.useState(true);
  const [products, setProducts] = React.useState<Product[]>([]);

  React.useEffect(() => {
    const fetchDeals = async () => {
      setIsLoading(true);
      try {
        // Fetch products with discounts
        const response = await productService.getProducts({
          page: 0,
          size: 30,
          sortBy: 'discountPrice',
          sortOrder: 'desc'
        });
        // Filter products with discounts
        const dealsProducts = response.content.filter(product => product.discountPrice && product.discountPrice > 0);
        setProducts(dealsProducts);
      } catch (error) {
        console.error('Failed to fetch deals:', error instanceof Error ? error.message : String(error));
        setProducts([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDeals();
  }, []);

  return (
    <Container className="py-12">
      {/* Header */}
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
          Special Deals & Offers
        </h1>
        <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
          Discover amazing discounts on our top products
        </p>
      </div>

      {/* Deals Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array.from({ length: 12 }).map((_, i) => (
            <ProductCardSkeleton key={i} />
          ))}
        </div>
      ) : products.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
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
          <h3 className="text-lg font-medium text-secondary-900 dark:text-secondary-100 mb-2">
            No deals available
          </h3>
          <p className="text-secondary-600 dark:text-secondary-400 mb-4">
            Check back later for new deals and discounts
          </p>
          <Button onClick={() => navigate('/products')}>
            Browse All Products
          </Button>
        </div>
      )}
    </Container>
  );
};

export default DealsPage;
