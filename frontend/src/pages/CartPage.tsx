import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, ArrowRight, Trash2, Heart, Minus, Plus } from 'lucide-react';
import { Container, Button, Badge, PriceDisplay } from '@/components';
import { CartItem } from '@/components/ecommerce';

// Mock cart data - in real app, this would come from context/API
const mockCartItems = [
  {
    productId: '1',
    productName: 'Wireless Bluetooth Headphones',
    brand: 'TechSound',
    price: 149.99,
    quantity: 1,
    maxQuantity: 25,
    imageUrl: '/api/placeholder/100/100',
    size: 'One Size',
    color: 'Black',
    variant: 'Standard',
    subtotal: 149.99,
    isAvailable: true
  },
  {
    productId: '2',
    productName: 'Smart Fitness Watch',
    brand: 'FitTech',
    price: 299.99,
    quantity: 2,
    maxQuantity: 15,
    imageUrl: '/api/placeholder/100/100',
    size: 'Medium',
    color: 'Blue',
    variant: 'GPS',
    subtotal: 599.98,
    isAvailable: true
  },
  {
    productId: '3',
    productName: 'Wireless Charging Pad',
    brand: 'PowerUp',
    price: 39.99,
    quantity: 1,
    maxQuantity: 50,
    imageUrl: '/api/placeholder/100/100',
    size: 'Standard',
    color: 'White',
    variant: 'Fast Charge',
    subtotal: 39.99,
    isAvailable: false
  }
];

export const CartPage: React.FC = () => {
  const [cartItems, setCartItems] = React.useState(mockCartItems);
  const [isLoading, setIsLoading] = React.useState(false);

  // Calculate totals
  const subtotal = cartItems.reduce((sum, item) => sum + item.subtotal, 0);
  const shipping = subtotal > 50 ? 0 : 9.99;
  const tax = subtotal * 0.08; // 8% tax
  const total = subtotal + shipping + tax;

  const handleUpdateQuantity = (productId: string, newQuantity: number) => {
    setCartItems(prev => 
      prev.map(item => 
        item.productId === productId 
          ? { ...item, quantity: newQuantity, subtotal: item.price * newQuantity }
          : item
      )
    );
  };

  const handleRemoveItem = (productId: string) => {
    setCartItems(prev => prev.filter(item => item.productId !== productId));
  };

  const handleMoveToWishlist = (productId: string) => {
    // Move item to wishlist
    console.log('Move to wishlist:', productId);
    handleRemoveItem(productId);
  };

  const handleClearCart = () => {
    setCartItems([]);
  };

  const handleCheckout = () => {
    setIsLoading(true);
    // Simulate checkout process
    setTimeout(() => {
      console.log('Proceeding to checkout');
      setIsLoading(false);
    }, 1000);
  };

  if (cartItems.length === 0) {
    return (
      <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
        <Container className="py-16">
          <div className="text-center">
            <div className="w-24 h-24 bg-secondary-200 dark:bg-secondary-700 rounded-full flex items-center justify-center mx-auto mb-6">
              <ShoppingBag className="h-12 w-12 text-secondary-400" />
            </div>
            <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Your cart is empty
            </h1>
            <p className="text-lg text-secondary-600 dark:text-secondary-400 mb-8 max-w-md mx-auto">
              Looks like you haven't added any items to your cart yet. Start shopping to fill it up!
            </p>
            <Link to="/products">
              <Button size="lg">
                Start Shopping
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </Link>
          </div>
        </Container>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100">
            Shopping Cart
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-secondary-600 dark:text-secondary-400">
              {cartItems.length} item{cartItems.length !== 1 ? 's' : ''}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={handleClearCart}
              className="text-error-600 hover:text-error-700 hover:bg-error-50"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Clear Cart
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {cartItems.map((item) => (
              <CartItem
                key={item.productId}
                item={item}
                onUpdateQuantity={handleUpdateQuantity}
                onRemove={handleRemoveItem}
                onMoveToWishlist={handleMoveToWishlist}
                variant="default"
              />
            ))}
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-6 sticky top-8">
              <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-6">
                Order Summary
              </h2>

              <div className="space-y-4 mb-6">
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Subtotal</span>
                  <span className="font-medium text-secondary-900 dark:text-secondary-100">
                    ${subtotal.toFixed(2)}
                  </span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Shipping</span>
                  <span className="font-medium text-secondary-900 dark:text-secondary-100">
                    {shipping === 0 ? (
                      <span className="text-success-600">Free</span>
                    ) : (
                      `$${shipping.toFixed(2)}`
                    )}
                  </span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Tax</span>
                  <span className="font-medium text-secondary-900 dark:text-secondary-100">
                    ${tax.toFixed(2)}
                  </span>
                </div>
                
                <div className="border-t border-secondary-200 dark:border-secondary-700 pt-4">
                  <div className="flex justify-between">
                    <span className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                      Total
                    </span>
                    <span className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                      ${total.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>

              {/* Shipping Progress */}
              {shipping > 0 && (
                <div className="bg-primary-50 dark:bg-primary-900/20 rounded-lg p-4 mb-6">
                  <div className="flex items-center gap-2 mb-2">
                    <div className="w-2 h-2 bg-primary-600 rounded-full"></div>
                    <span className="text-sm font-medium text-primary-800 dark:text-primary-200">
                      Free shipping available
                    </span>
                  </div>
                  <p className="text-sm text-primary-700 dark:text-primary-300">
                    Add ${(50 - subtotal).toFixed(2)} more to get free shipping
                  </p>
                </div>
              )}

              {/* Checkout Button */}
              <Button
                size="lg"
                className="w-full mb-4"
                onClick={handleCheckout}
                loading={isLoading}
                disabled={cartItems.some(item => !item.isAvailable)}
              >
                Proceed to Checkout
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>

              {/* Continue Shopping */}
              <Link to="/products" className="block">
                <Button variant="outline" size="lg" className="w-full">
                  Continue Shopping
                </Button>
              </Link>

              {/* Security Badge */}
              <div className="mt-6 pt-6 border-t border-secondary-200 dark:border-secondary-700">
                <div className="flex items-center justify-center gap-2 text-sm text-secondary-600 dark:text-secondary-400">
                  <div className="w-4 h-4 bg-success-500 rounded-full flex items-center justify-center">
                    <div className="w-2 h-2 bg-white rounded-full"></div>
                  </div>
                  Secure checkout with SSL encryption
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Recently Viewed / Recommendations */}
        <div className="mt-16">
          <h2 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100 mb-8">
            You might also like
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Placeholder for recommended products */}
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-4">
                <div className="aspect-square bg-secondary-100 dark:bg-secondary-700 rounded-lg mb-4"></div>
                <h3 className="font-medium text-secondary-900 dark:text-secondary-100 mb-2">
                  Recommended Product {i + 1}
                </h3>
                <p className="text-sm text-secondary-600 dark:text-secondary-400 mb-3">
                  Great addition to your cart
                </p>
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-primary-600">$99.99</span>
                  <Button size="sm">Add to Cart</Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Container>
    </div>
  );
};