import React from 'react';
import { useNavigate } from 'react-router-dom';
import { X, ShoppingBag, Plus, Minus, Trash2, ArrowRight } from 'lucide-react';
import { Button } from '@/components';
import { useCart } from '@/contexts/CartContext';
import { cn } from '@/utils/cn';

interface CartDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export const CartDrawer: React.FC<CartDrawerProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();
  const { state, updateQuantity, removeFromCart, clearCart } = useCart();

  const handleCheckout = () => {
    onClose();
    navigate('/checkout');
  };

  const handleContinueShopping = () => {
    onClose();
    navigate('/products');
  };

  return (
    <>
      {/* Backdrop */}
      <div
        className={cn(
          'fixed inset-0 bg-black/50 z-40 transition-opacity duration-300',
          isOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'
        )}
        onClick={onClose}
      />

      {/* Drawer */}
      <div
        className={cn(
          'fixed top-0 right-0 h-full w-full sm:w-96 bg-white dark:bg-secondary-900 shadow-xl z-50 transform transition-transform duration-300 ease-in-out',
          isOpen ? 'translate-x-0' : 'translate-x-full'
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-secondary-200 dark:border-secondary-700">
          <h2 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
            Shopping Cart ({state.totalItems} items)
          </h2>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        {/* Cart Items */}
        <div className="flex-1 overflow-y-auto p-4">
          {state.items.length === 0 ? (
            <div className="text-center py-8">
              <ShoppingBag className="h-12 w-12 mx-auto text-secondary-400 mb-4" />
              <h3 className="text-lg font-medium text-secondary-900 dark:text-secondary-100 mb-2">
                Your cart is empty
              </h3>
              <p className="text-secondary-600 dark:text-secondary-400 mb-4">
                Add some items to your cart to get started
              </p>
              <Button onClick={handleContinueShopping}>
                Continue Shopping
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {state.items.map((item) => (
                <div
                  key={item.productId}
                  className="flex gap-4 p-3 bg-secondary-50 dark:bg-secondary-800 rounded-lg"
                >
                  {/* Product Image */}
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-20 h-20 object-cover rounded-md"
                  />

                  {/* Product Info */}
                  <div className="flex-1">
                    <h3 className="font-medium text-secondary-900 dark:text-secondary-100 mb-1">
                      {item.name}
                    </h3>
                    <p className="text-sm text-secondary-600 dark:text-secondary-400 mb-2">
                      ${item.price.toFixed(2)}
                    </p>

                    {/* Quantity Controls */}
                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="icon-sm"
                        onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                        disabled={item.quantity <= 1}
                      >
                        <Minus className="h-4 w-4" />
                      </Button>
                      <span className="w-8 text-center">{item.quantity}</span>
                      <Button
                        variant="outline"
                        size="icon-sm"
                        onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                        disabled={item.quantity >= item.stock}
                      >
                        <Plus className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        className="ml-2 text-error-600 hover:text-error-700"
                        onClick={() => removeFromCart(item.productId)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>

                  {/* Item Total */}
                  <div className="text-right">
                    <p className="font-medium text-secondary-900 dark:text-secondary-100">
                      ${(item.price * item.quantity).toFixed(2)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {state.items.length > 0 && (
          <div className="border-t border-secondary-200 dark:border-secondary-700 p-4 space-y-4">
            {/* Summary */}
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-secondary-600 dark:text-secondary-400">Subtotal</span>
                <span className="font-medium text-secondary-900 dark:text-secondary-100">
                  ${state.totalAmount.toFixed(2)}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-secondary-600 dark:text-secondary-400">Shipping</span>
                <span className="font-medium text-secondary-900 dark:text-secondary-100">
                  {state.totalAmount >= 50 ? 'Free' : '$4.99'}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-secondary-600 dark:text-secondary-400">Tax</span>
                <span className="font-medium text-secondary-900 dark:text-secondary-100">
                  ${(state.totalAmount * 0.1).toFixed(2)}
                </span>
              </div>
              <div className="border-t border-secondary-200 dark:border-secondary-700 pt-2 flex justify-between">
                <span className="font-medium text-secondary-900 dark:text-secondary-100">Total</span>
                <span className="font-bold text-lg text-primary-600 dark:text-primary-400">
                  ${(state.totalAmount + (state.totalAmount >= 50 ? 0 : 4.99) + state.totalAmount * 0.1).toFixed(2)}
                </span>
              </div>
            </div>

            {/* Actions */}
            <div className="space-y-2">
              <Button className="w-full" onClick={handleCheckout}>
                Proceed to Checkout
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
              <Button variant="outline" className="w-full" onClick={handleContinueShopping}>
                Continue Shopping
              </Button>
            </div>
          </div>
        )}
      </div>
    </>
  );
};
