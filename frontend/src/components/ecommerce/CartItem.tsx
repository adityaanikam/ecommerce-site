import React from 'react';
import { Minus, Plus, Trash2, Heart } from 'lucide-react';
import { Button, Badge } from '@/components/ui';
import { PriceDisplay, RatingStars } from './';
import { cn } from '@/utils/cn';
import { getImageUrl, handleImageError } from '@/config';
import { CartItem as CartItemType } from '@/types';

export interface CartItemProps {
  item: CartItemType;
  onUpdateQuantity: (productId: string, quantity: number) => void;
  onRemove: (productId: string) => void;
  onMoveToWishlist?: (productId: string) => void;
  className?: string;
  variant?: 'default' | 'compact';
  showActions?: boolean;
}

export const CartItem: React.FC<CartItemProps> = ({
  item,
  onUpdateQuantity,
  onRemove,
  onMoveToWishlist,
  className,
  variant = 'default',
  showActions = true,
}) => {
  const [imageError, setImageError] = React.useState(false);

  const handleQuantityChange = (newQuantity: number) => {
    if (newQuantity >= 1 && newQuantity <= item.stock) {
      onUpdateQuantity(item.productId, newQuantity);
    }
  };

  if (variant === 'compact') {
    return (
      <div className={cn('flex items-center space-x-3 p-3 border border-secondary-200 rounded-lg', className)}>
        {/* Image */}
        <div className="w-12 h-12 flex-shrink-0">
          {item.image && !imageError ? (
            <img
              src={getImageUrl(item.image)}
              alt={item.name}
              className="w-full h-full object-cover rounded-md"
              onError={(e) => {
                setImageError(true);
                handleImageError(e, '200x200');
              }}
            />
          ) : (
            <div className="w-full h-full bg-secondary-100 rounded-md flex items-center justify-center">
              <span className="text-secondary-400 text-xs">No Image</span>
            </div>
          )}
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <h4 className="font-medium text-secondary-900 truncate">
            {item.name}
          </h4>
          <div className="flex items-center justify-between mt-1">
            <PriceDisplay
              price={item.price}
              size="sm"
            />
            <span className="text-sm text-secondary-600">
              Qty: {item.quantity}
            </span>
          </div>
        </div>

        {/* Actions */}
        {showActions && (
          <Button
            size="icon-sm"
            variant="ghost"
            onClick={() => onRemove(item.productId)}
            className="text-error-600 hover:text-error-700 hover:bg-error-50"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        )}
      </div>
    );
  }

  // Default variant
  return (
    <div className={cn('flex items-center space-x-4 p-4 border border-secondary-200 rounded-lg', className)}>
      {/* Image */}
      <div className="w-20 h-20 flex-shrink-0">
        {item.image && !imageError ? (
          <img
            src={getImageUrl(item.image)}
            alt={item.name}
            className="w-full h-full object-cover rounded-lg"
            onError={(e) => {
              setImageError(true);
              handleImageError(e, '400x400');
            }}
          />
        ) : (
          <div className="w-full h-full bg-secondary-100 rounded-lg flex items-center justify-center">
            <span className="text-secondary-400 text-sm">No Image</span>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <h4 className="font-semibold text-secondary-900 line-clamp-2">
              {item.name}
            </h4>
          </div>

          {/* Price */}
          <div className="text-right ml-4">
            <PriceDisplay
              price={item.price}
              size="md"
            />
            <p className="text-sm text-secondary-600 mt-1">
              Total: ${(item.price * item.quantity).toFixed(2)}
            </p>
          </div>
        </div>

        {/* Actions */}
        {showActions && (
          <div className="flex items-center justify-between mt-4">
            {/* Quantity Controls */}
            <div className="flex items-center space-x-2">
              <Button
                size="icon-sm"
                variant="outline"
                onClick={() => handleQuantityChange(item.quantity - 1)}
                disabled={item.quantity <= 1}
              >
                <Minus className="h-3 w-3" />
              </Button>
              
              <span className="w-8 text-center text-sm font-medium">
                {item.quantity}
              </span>
              
              <Button
                size="icon-sm"
                variant="outline"
                onClick={() => handleQuantityChange(item.quantity + 1)}
                disabled={item.quantity >= item.stock}
              >
                <Plus className="h-3 w-3" />
              </Button>
            </div>

            {/* Action Buttons */}
            <div className="flex items-center space-x-2">
              {onMoveToWishlist && (
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => onMoveToWishlist(item.productId)}
                  className="text-secondary-600 hover:text-primary-600"
                >
                  <Heart className="h-4 w-4 mr-1" />
                  Save
                </Button>
              )}
              
              <Button
                size="sm"
                variant="ghost"
                onClick={() => onRemove(item.productId)}
                className="text-error-600 hover:text-error-700 hover:bg-error-50"
              >
                <Trash2 className="h-4 w-4 mr-1" />
                Remove
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
