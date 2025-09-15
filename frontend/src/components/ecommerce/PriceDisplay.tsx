import React from 'react';
import { cn } from '@/utils/cn';

export interface PriceDisplayProps {
  price: number;
  discountPrice?: number;
  currency?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showOriginal?: boolean;
  showDiscount?: boolean;
  className?: string;
}

const sizeClasses = {
  sm: {
    current: 'text-sm',
    original: 'text-xs',
    discount: 'text-xs',
  },
  md: {
    current: 'text-base',
    original: 'text-sm',
    discount: 'text-sm',
  },
  lg: {
    current: 'text-lg',
    original: 'text-base',
    discount: 'text-base',
  },
  xl: {
    current: 'text-xl',
    original: 'text-lg',
    discount: 'text-lg',
  },
};

export const PriceDisplay: React.FC<PriceDisplayProps> = ({
  price,
  discountPrice,
  currency = '$',
  size = 'md',
  showOriginal = true,
  showDiscount = true,
  className,
}) => {
  const hasDiscount = discountPrice && discountPrice < price;
  const discountPercentage = hasDiscount 
    ? Math.round(((price - discountPrice!) / price) * 100)
    : 0;

  const currentPrice = hasDiscount ? discountPrice! : price;
  const originalPrice = hasDiscount ? price : null;

  return (
    <div className={cn('flex items-center space-x-2', className)}>
      {/* Current Price */}
      <span className={cn(
        'font-bold text-primary-600',
        sizeClasses[size].current
      )}>
        {currency}{currentPrice.toFixed(2)}
      </span>

      {/* Original Price */}
      {originalPrice && showOriginal && (
        <span className={cn(
          'text-secondary-500 line-through',
          sizeClasses[size].original
        )}>
          {currency}{originalPrice.toFixed(2)}
        </span>
      )}

      {/* Discount Badge */}
      {hasDiscount && showDiscount && (
        <span className={cn(
          'bg-error-100 text-error-800 px-2 py-1 rounded-full font-medium',
          sizeClasses[size].discount
        )}>
          -{discountPercentage}%
        </span>
      )}
    </div>
  );
};
