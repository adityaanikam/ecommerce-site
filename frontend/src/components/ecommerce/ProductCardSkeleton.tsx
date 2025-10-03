import React from 'react';
import { Card, CardContent } from '@/components';
import { cn } from '@/utils/cn';

interface ProductCardSkeletonProps {
  className?: string;
}

export const ProductCardSkeleton: React.FC<ProductCardSkeletonProps> = ({
  className,
}) => {
  return (
    <Card
      className={cn(
        'overflow-hidden animate-pulse',
        className
      )}
    >
      <CardContent className="p-0">
        {/* Image skeleton */}
        <div className="aspect-square bg-gradient-to-r from-secondary-100 via-secondary-200 to-secondary-100 dark:from-secondary-700 dark:via-secondary-600 dark:to-secondary-700 relative overflow-hidden">
          <div
            className="absolute inset-0"
            style={{
              backgroundImage: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent)',
              animation: 'shimmer 1.5s infinite',
              transform: 'translateX(-100%)',
            }}
          />
        </div>

        {/* Content skeleton */}
        <div className="p-4 space-y-4">
          {/* Title */}
          <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded-md w-3/4" />
          
          {/* Brand */}
          <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded-md w-1/2" />

          {/* Rating */}
          <div className="flex items-center gap-2">
            <div className="flex gap-1">
              {Array.from({ length: 5 }).map((_, i) => (
                <div
                  key={i}
                  className="w-4 h-4 rounded-full bg-secondary-200 dark:bg-secondary-700"
                />
              ))}
            </div>
            <div className="h-4 w-10 bg-secondary-200 dark:bg-secondary-700 rounded-md" />
          </div>

          {/* Price */}
          <div className="flex items-center gap-2">
            <div className="h-6 w-20 bg-secondary-200 dark:bg-secondary-700 rounded-md" />
            <div className="h-4 w-16 bg-secondary-200 dark:bg-secondary-700 rounded-md" />
          </div>

          {/* Buttons */}
          <div className="space-y-2 pt-2">
            <div className="h-10 bg-secondary-200 dark:bg-secondary-700 rounded-md" />
            <div className="h-10 bg-secondary-200 dark:bg-secondary-700 rounded-md" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
