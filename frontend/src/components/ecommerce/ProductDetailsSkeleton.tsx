import React from 'react';
import { Card, CardContent } from '@/components';
import { cn } from '@/utils/cn';

interface ProductDetailsSkeletonProps {
  className?: string;
}

export const ProductDetailsSkeleton: React.FC<ProductDetailsSkeletonProps> = ({
  className,
}) => {
  return (
    <div className={cn('animate-pulse', className)}>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Image Gallery Skeleton */}
        <div className="space-y-4">
          <div className="aspect-square bg-secondary-200 dark:bg-secondary-700 rounded-lg" />
          <div className="grid grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="aspect-square bg-secondary-200 dark:bg-secondary-700 rounded-lg"
              />
            ))}
          </div>
        </div>

        {/* Product Info Skeleton */}
        <div className="space-y-6">
          {/* Title and Brand */}
          <div className="space-y-4">
            <div className="h-8 bg-secondary-200 dark:bg-secondary-700 rounded-md w-3/4" />
            <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-1/2" />
          </div>

          {/* Price */}
          <div className="flex items-center gap-4">
            <div className="h-8 bg-secondary-200 dark:bg-secondary-700 rounded-md w-32" />
            <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded-md w-24" />
          </div>

          {/* Description */}
          <div className="space-y-2">
            <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-full" />
            <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-5/6" />
            <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-4/6" />
          </div>

          {/* Stock Status */}
          <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded-md w-24" />

          {/* Quantity Selector */}
          <div className="h-12 bg-secondary-200 dark:bg-secondary-700 rounded-md w-32" />

          {/* Add to Cart Button */}
          <div className="h-12 bg-secondary-200 dark:bg-secondary-700 rounded-md w-full" />

          {/* Tabs */}
          <Card>
            <CardContent className="p-6 space-y-4">
              <div className="flex gap-4">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div
                    key={i}
                    className="h-8 bg-secondary-200 dark:bg-secondary-700 rounded-md w-24"
                  />
                ))}
              </div>
              <div className="space-y-2">
                <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-full" />
                <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-5/6" />
                <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-4/6" />
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
