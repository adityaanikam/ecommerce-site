import React from 'react';
import { Skeleton, SkeletonText, SkeletonAvatar, SkeletonButton, SkeletonCard } from '@/components/ui';

// Product card skeleton
export const ProductCardSkeleton: React.FC = () => (
  <SkeletonCard className="p-4">
    <div className="space-y-4">
      <Skeleton className="aspect-square w-full rounded-lg" />
      <div className="space-y-2">
        <SkeletonText className="h-4 w-3/4" />
        <SkeletonText className="h-3 w-1/2" />
        <div className="flex items-center justify-between">
          <SkeletonText className="h-4 w-1/3" />
          <SkeletonButton className="h-8 w-20" />
        </div>
      </div>
    </div>
  </SkeletonCard>
);

// Product list skeleton
export const ProductListSkeleton: React.FC<{ count?: number }> = ({ count = 8 }) => (
  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
    {Array.from({ length: count }).map((_, i) => (
      <ProductCardSkeleton key={i} />
    ))}
  </div>
);

// Product detail skeleton
export const ProductDetailSkeleton: React.FC = () => (
  <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
    {/* Image section */}
    <div className="space-y-4">
      <Skeleton className="aspect-square w-full rounded-lg" />
      <div className="grid grid-cols-5 gap-2">
        {Array.from({ length: 5 }).map((_, i) => (
          <Skeleton key={i} className="aspect-square rounded-lg" />
        ))}
      </div>
    </div>

    {/* Info section */}
    <div className="space-y-6">
      <div className="space-y-2">
        <SkeletonText className="h-4 w-1/4" />
        <SkeletonText className="h-8 w-3/4" />
        <SkeletonText className="h-4 w-1/2" />
        <SkeletonText className="h-6 w-1/3" />
      </div>

      <div className="space-y-2">
        <SkeletonText className="h-4 w-full" />
        <SkeletonText className="h-4 w-5/6" />
        <SkeletonText className="h-4 w-4/6" />
      </div>

      <div className="space-y-3">
        <SkeletonText className="h-5 w-1/3" />
        <div className="grid grid-cols-2 gap-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <SkeletonText key={i} className="h-3 w-full" />
          ))}
        </div>
      </div>

      <div className="space-y-3">
        <SkeletonText className="h-5 w-1/4" />
        <div className="flex gap-2">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="w-10 h-10 rounded-full" />
          ))}
        </div>
      </div>

      <div className="space-y-3">
        <SkeletonText className="h-5 w-1/3" />
        <div className="flex items-center gap-4">
          <div className="flex items-center border rounded-lg">
            <SkeletonButton className="h-8 w-8" />
            <SkeletonText className="h-8 w-16" />
            <SkeletonButton className="h-8 w-8" />
          </div>
          <SkeletonText className="h-4 w-20" />
        </div>
      </div>

      <div className="flex gap-4">
        <SkeletonButton className="h-12 flex-1" />
        <SkeletonButton className="h-12 w-12" />
        <SkeletonButton className="h-12 w-12" />
      </div>
    </div>
  </div>
);

// Cart item skeleton
export const CartItemSkeleton: React.FC = () => (
  <div className="flex items-center gap-4 p-4 border border-secondary-200 dark:border-secondary-700 rounded-lg">
    <Skeleton className="w-16 h-16 rounded" />
    <div className="flex-1 space-y-2">
      <SkeletonText className="h-4 w-3/4" />
      <SkeletonText className="h-3 w-1/2" />
      <SkeletonText className="h-3 w-1/3" />
    </div>
    <div className="flex items-center gap-2">
      <SkeletonButton className="h-8 w-8" />
      <SkeletonText className="h-8 w-16" />
      <SkeletonButton className="h-8 w-8" />
    </div>
    <div className="text-right space-y-1">
      <SkeletonText className="h-4 w-16" />
      <SkeletonText className="h-3 w-12" />
    </div>
  </div>
);

// Cart skeleton
export const CartSkeleton: React.FC = () => (
  <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div className="lg:col-span-2 space-y-4">
      {Array.from({ length: 3 }).map((_, i) => (
        <CartItemSkeleton key={i} />
      ))}
    </div>
    <div className="lg:col-span-1">
      <SkeletonCard className="p-6">
        <div className="space-y-4">
          <SkeletonText className="h-6 w-1/3" />
          <div className="space-y-3">
            <div className="flex justify-between">
              <SkeletonText className="h-4 w-20" />
              <SkeletonText className="h-4 w-16" />
            </div>
            <div className="flex justify-between">
              <SkeletonText className="h-4 w-20" />
              <SkeletonText className="h-4 w-16" />
            </div>
            <div className="flex justify-between">
              <SkeletonText className="h-4 w-20" />
              <SkeletonText className="h-4 w-16" />
            </div>
          </div>
          <div className="border-t pt-4">
            <div className="flex justify-between">
              <SkeletonText className="h-5 w-16" />
              <SkeletonText className="h-5 w-20" />
            </div>
          </div>
          <SkeletonButton className="h-12 w-full" />
        </div>
      </SkeletonCard>
    </div>
  </div>
);

// Order item skeleton
export const OrderItemSkeleton: React.FC = () => (
  <div className="flex items-center gap-4 p-4 border border-secondary-200 dark:border-secondary-700 rounded-lg">
    <Skeleton className="w-16 h-16 rounded" />
    <div className="flex-1 space-y-2">
      <SkeletonText className="h-4 w-3/4" />
      <SkeletonText className="h-3 w-1/2" />
      <SkeletonText className="h-3 w-1/3" />
    </div>
    <div className="text-right space-y-1">
      <SkeletonText className="h-4 w-16" />
      <SkeletonText className="h-3 w-12" />
    </div>
  </div>
);

// Order list skeleton
export const OrderListSkeleton: React.FC<{ count?: number }> = ({ count = 5 }) => (
  <div className="space-y-4">
    {Array.from({ length: count }).map((_, i) => (
      <SkeletonCard key={i} className="p-6">
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <SkeletonText className="h-5 w-32" />
            <SkeletonText className="h-4 w-20" />
          </div>
          <div className="space-y-2">
            {Array.from({ length: 2 }).map((_, j) => (
              <OrderItemSkeleton key={j} />
            ))}
          </div>
          <div className="flex items-center justify-between pt-4 border-t">
            <SkeletonText className="h-4 w-24" />
            <SkeletonButton className="h-8 w-24" />
          </div>
        </div>
      </SkeletonCard>
    ))}
  </div>
);

// User profile skeleton
export const UserProfileSkeleton: React.FC = () => (
  <div className="space-y-6">
    <div className="flex items-center gap-4">
      <SkeletonAvatar className="w-20 h-20" />
      <div className="space-y-2">
        <SkeletonText className="h-6 w-48" />
        <SkeletonText className="h-4 w-32" />
        <SkeletonText className="h-4 w-24" />
      </div>
    </div>

    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <SkeletonCard className="p-6">
        <div className="space-y-4">
          <SkeletonText className="h-5 w-1/3" />
          <div className="space-y-3">
            <SkeletonText className="h-4 w-full" />
            <SkeletonText className="h-4 w-5/6" />
            <SkeletonText className="h-4 w-4/6" />
          </div>
        </div>
      </SkeletonCard>

      <SkeletonCard className="p-6">
        <div className="space-y-4">
          <SkeletonText className="h-5 w-1/3" />
          <div className="space-y-3">
            <SkeletonText className="h-4 w-full" />
            <SkeletonText className="h-4 w-5/6" />
            <SkeletonText className="h-4 w-4/6" />
          </div>
        </div>
      </SkeletonCard>
    </div>
  </div>
);

// Table skeleton
export const TableSkeleton: React.FC<{ rows?: number; cols?: number }> = ({ rows = 5, cols = 4 }) => (
  <div className="overflow-hidden border border-secondary-200 dark:border-secondary-700 rounded-lg">
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="bg-secondary-50 dark:bg-secondary-800">
          <tr>
            {Array.from({ length: cols }).map((_, i) => (
              <th key={i} className="px-6 py-3 text-left">
                <SkeletonText className="h-4 w-20" />
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-secondary-200 dark:divide-secondary-700">
          {Array.from({ length: rows }).map((_, i) => (
            <tr key={i}>
              {Array.from({ length: cols }).map((_, j) => (
                <td key={j} className="px-6 py-4">
                  <SkeletonText className="h-4 w-16" />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
);

// Page skeleton
export const PageSkeleton: React.FC = () => (
  <div className="space-y-6">
    <div className="space-y-2">
      <SkeletonText className="h-8 w-1/3" />
      <SkeletonText className="h-4 w-1/2" />
    </div>
    <SkeletonCard className="p-6">
      <div className="space-y-4">
        <SkeletonText className="h-6 w-1/4" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <SkeletonText className="h-4 w-full" />
          <SkeletonText className="h-4 w-full" />
        </div>
        <SkeletonText className="h-4 w-3/4" />
      </div>
    </SkeletonCard>
  </div>
);

export default {
  ProductCardSkeleton,
  ProductListSkeleton,
  ProductDetailSkeleton,
  CartItemSkeleton,
  CartSkeleton,
  OrderItemSkeleton,
  OrderListSkeleton,
  UserProfileSkeleton,
  TableSkeleton,
  PageSkeleton,
};
