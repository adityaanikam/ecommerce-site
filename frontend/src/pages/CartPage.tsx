import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button } from '@/components';
import { useCart } from '@/contexts/CartContext';
import { formatCurrency } from '@/utils/format';

const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const { state: cart, updateQuantity, removeFromCart } = useCart();

  if (cart.items.length === 0) {
    return (
      <Container className="py-12">
          <div className="text-center">
          <h1 className="text-2xl font-bold text-secondary-900 dark:text-white mb-4">
            Your Cart is Empty
            </h1>
          <p className="text-secondary-600 dark:text-secondary-400 mb-8">
            Looks like you haven't added any items to your cart yet.
          </p>
          <Button onClick={() => navigate('/products')}>
            Continue Shopping
              </Button>
          </div>
        </Container>
    );
  }

  return (
    <Container className="py-12">
      <h1 className="text-2xl font-bold text-secondary-900 dark:text-white mb-8">
        Shopping Cart ({cart.totalItems} items)
          </h1>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Cart Items */}
        <div className="lg:col-span-8">
          <div className="space-y-4">
            {cart.items.map((item) => (
              <div
                key={item.productId}
                className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-4 flex items-center gap-4"
              >
                <img
                  src={item.image}
                  alt={item.name}
                  className="w-24 h-24 object-cover rounded-md"
                />
                
                <div className="flex-grow">
                  <h3 className="font-medium text-secondary-900 dark:text-white">
                    {item.name}
                  </h3>
                  <p className="text-secondary-600 dark:text-secondary-400">
                    {formatCurrency(item.price)}
                  </p>
                </div>
                
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => updateQuantity(item.productId, Math.max(0, item.quantity - 1))}
                  >
                    -
                  </Button>
                  <span className="w-12 text-center">{item.quantity}</span>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => updateQuantity(item.productId, Math.min(item.stock, item.quantity + 1))}
                  >
                    +
                  </Button>
                </div>
                
                <div className="text-right">
                  <p className="font-medium text-secondary-900 dark:text-white">
                    {formatCurrency(item.price * item.quantity)}
                  </p>
                  <button
                    onClick={() => removeFromCart(item.productId)}
                    className="text-sm text-error-600 hover:text-error-700 dark:text-error-400 dark:hover:text-error-300"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-4">
          <div className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-semibold text-secondary-900 dark:text-white mb-4">
              Order Summary
          </h2>

            <div className="space-y-2 mb-4">
              <div className="flex justify-between text-secondary-600 dark:text-secondary-400">
                <span>Subtotal</span>
                <span>{formatCurrency(cart.totalAmount)}</span>
              </div>
              <div className="flex justify-between text-secondary-600 dark:text-secondary-400">
                <span>Shipping</span>
                <span>Free</span>
              </div>
              <div className="flex justify-between text-secondary-600 dark:text-secondary-400">
                <span>Tax</span>
                <span>{formatCurrency(cart.totalAmount * 0.1)}</span>
              </div>
              <div className="border-t border-secondary-200 dark:border-secondary-700 pt-2 mt-2">
                <div className="flex justify-between font-semibold text-secondary-900 dark:text-white">
                  <span>Total</span>
                  <span>{formatCurrency(cart.totalAmount * 1.1)}</span>
                </div>
              </div>
            </div>

            <Button
              className="w-full"
              onClick={() => navigate('/checkout')}
            >
              Proceed to Checkout
            </Button>

            <button
              onClick={() => navigate('/products')}
              className="mt-4 text-center w-full text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300"
            >
              Continue Shopping
            </button>
          </div>
          </div>
        </div>
      </Container>
  );
};

export default CartPage;