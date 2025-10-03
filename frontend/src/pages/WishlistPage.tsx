import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart } from 'lucide-react';
import { Container, Button } from '@/components';
import { ProductCard } from '@/components/ecommerce';
import { useWishlist } from '@/contexts/WishlistContext';
import { useCart } from '@/contexts/CartContext';

export const WishlistPage: React.FC = () => {
  const navigate = useNavigate();
  const { state: wishlistState, removeFromWishlist } = useWishlist();
  const { addToCart } = useCart();

  if (wishlistState.items.length === 0) {
    return (
      <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
        <Container className="py-8">
          <div className="text-center">
            <div className="inline-block p-4 rounded-full bg-secondary-100 dark:bg-secondary-800 mb-4">
              <Heart className="h-8 w-8 text-secondary-400" />
            </div>
            <h2 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100 mb-2">
              Your wishlist is empty
            </h2>
            <p className="text-secondary-600 dark:text-secondary-400 mb-6">
              Add items to your wishlist to save them for later
            </p>
            <Button onClick={() => navigate('/products')}>
              Browse Products
            </Button>
          </div>
        </Container>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-2">
              My Wishlist
            </h1>
            <p className="text-secondary-600 dark:text-secondary-400">
              {wishlistState.items.length} items saved
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {wishlistState.items.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToCart={(id, quantity) => {
                const product = wishlistState.items.find(p => p.id === id);
                if (product) {
                  addToCart(product, quantity);
                  removeFromWishlist(id);
                }
              }}
              onAddToWishlist={(id) => removeFromWishlist(id)}
              onQuickView={(id) => navigate(`/products/${id}`)}
              onViewDetails={(id) => navigate(`/products/${id}`)}
            />
          ))}
        </div>
      </Container>
    </div>
  );
};

export default WishlistPage;