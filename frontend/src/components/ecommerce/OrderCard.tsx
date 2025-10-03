import React from 'react';
import { Package, ChevronRight } from 'lucide-react';
import { Card, CardContent, Badge } from '@/components';
import { Order, OrderStatus } from '@/types/api';
import { cn } from '@/utils/cn';

interface OrderCardProps {
  order: Order;
  onClick?: (orderId: string) => void;
  className?: string;
}

const statusColors = {
  [OrderStatus.PENDING]: 'bg-warning-500',
  [OrderStatus.PROCESSING]: 'bg-info-500',
  [OrderStatus.SHIPPED]: 'bg-primary-500',
  [OrderStatus.DELIVERED]: 'bg-success-500',
  [OrderStatus.CANCELLED]: 'bg-error-500',
};

const statusLabels = {
  [OrderStatus.PENDING]: 'Pending',
  [OrderStatus.PROCESSING]: 'Processing',
  [OrderStatus.SHIPPED]: 'Shipped',
  [OrderStatus.DELIVERED]: 'Delivered',
  [OrderStatus.CANCELLED]: 'Cancelled',
};

export const OrderCard: React.FC<OrderCardProps> = ({
  order,
  onClick,
  className,
}) => {
  const formattedDate = new Date(order.createdAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <Card
      className={cn(
        'hover:shadow-md transition-shadow duration-200',
        onClick && 'cursor-pointer',
        className
      )}
      onClick={() => onClick?.(order.id)}
    >
      <CardContent className="p-4">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-3">
            <div className="flex-shrink-0">
              <Package className="h-5 w-5 text-primary-500" />
            </div>
            <div>
              <p className="text-sm font-medium text-secondary-900 dark:text-secondary-100">
                Order #{order.id}
              </p>
              <p className="text-sm text-secondary-500 dark:text-secondary-400">
                {formattedDate}
              </p>
            </div>
          </div>
          <Badge
            className={cn(
              'text-white',
              statusColors[order.status]
            )}
          >
            {statusLabels[order.status]}
          </Badge>
        </div>

        <div className="space-y-3">
          {order.items.map((item) => (
            <div
              key={item.productId}
              className="flex items-center space-x-3"
            >
              <img
                src={item.image}
                alt={item.name}
                className="w-12 h-12 rounded-md object-cover"
              />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-secondary-900 dark:text-secondary-100 truncate">
                  {item.name}
                </p>
                <p className="text-sm text-secondary-500 dark:text-secondary-400">
                  Qty: {item.quantity} × ${item.price.toFixed(2)}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-4 pt-4 border-t border-secondary-200 dark:border-secondary-700 flex items-center justify-between">
          <div>
            <p className="text-sm text-secondary-500 dark:text-secondary-400">
              Total Amount
            </p>
            <p className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
              ${order.totalAmount.toFixed(2)}
            </p>
          </div>
          {onClick && (
            <ChevronRight className="h-5 w-5 text-secondary-400" />
          )}
        </div>
      </CardContent>
    </Card>
  );
};