import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Package, Truck, CheckCircle, XCircle, Clock } from 'lucide-react';
import { Card, CardContent, Badge, Button } from '@/components/ui';
import { cn } from '@/utils/cn';
import { Order, OrderStatus } from '@/types';

export interface OrderCardProps {
  order: Order;
  className?: string;
  variant?: 'default' | 'compact';
  showActions?: boolean;
}

const statusConfig = {
  [OrderStatus.PENDING]: {
    icon: Clock,
    color: 'bg-warning-100 text-warning-800',
    label: 'Pending',
  },
  [OrderStatus.CONFIRMED]: {
    icon: CheckCircle,
    color: 'bg-primary-100 text-primary-800',
    label: 'Confirmed',
  },
  [OrderStatus.PROCESSING]: {
    icon: Package,
    color: 'bg-blue-100 text-blue-800',
    label: 'Processing',
  },
  [OrderStatus.SHIPPED]: {
    icon: Truck,
    color: 'bg-info-100 text-info-800',
    label: 'Shipped',
  },
  [OrderStatus.DELIVERED]: {
    icon: CheckCircle,
    color: 'bg-success-100 text-success-800',
    label: 'Delivered',
  },
  [OrderStatus.CANCELLED]: {
    icon: XCircle,
    color: 'bg-error-100 text-error-800',
    label: 'Cancelled',
  },
  [OrderStatus.REFUNDED]: {
    icon: XCircle,
    color: 'bg-secondary-100 text-secondary-800',
    label: 'Refunded',
  },
};

export const OrderCard: React.FC<OrderCardProps> = ({
  order,
  className,
  variant = 'default',
  showActions = true,
}) => {
  const statusInfo = statusConfig[order.status];
  const StatusIcon = statusInfo.icon;

  if (variant === 'compact') {
    return (
      <Link
        to={`/orders/${order.id}`}
        className={cn(
          'block p-4 border border-secondary-200 rounded-lg hover:shadow-md transition-all duration-200',
          className
        )}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="flex-shrink-0">
              <StatusIcon className="h-5 w-5 text-secondary-400" />
            </div>
            <div>
              <h4 className="font-medium text-secondary-900">
                Order #{order.orderNumber}
              </h4>
              <p className="text-sm text-secondary-500">
                {order.items.length} item{order.items.length !== 1 ? 's' : ''}
              </p>
            </div>
          </div>
          
          <div className="text-right">
            <Badge className={statusInfo.color}>
              {statusInfo.label}
            </Badge>
            <p className="text-sm font-medium text-secondary-900 mt-1">
              ${order.totalAmount.toFixed(2)}
            </p>
          </div>
        </div>
      </Link>
    );
  }

  // Default variant
  return (
    <Card className={cn('hover:shadow-md transition-all duration-200', className)}>
      <CardContent className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div>
            <h3 className="text-lg font-semibold text-secondary-900">
              Order #{order.orderNumber}
            </h3>
            <div className="flex items-center space-x-4 mt-1">
              <div className="flex items-center space-x-1 text-sm text-secondary-500">
                <Calendar className="h-4 w-4" />
                <span>
                  {new Date(order.createdAt).toLocaleDateString()}
                </span>
              </div>
              <div className="flex items-center space-x-1 text-sm text-secondary-500">
                <Package className="h-4 w-4" />
                <span>
                  {order.items.length} item{order.items.length !== 1 ? 's' : ''}
                </span>
              </div>
            </div>
          </div>
          
          <Badge className={statusInfo.color}>
            <StatusIcon className="h-3 w-3 mr-1" />
            {statusInfo.label}
          </Badge>
        </div>

        {/* Order Items Preview */}
        <div className="space-y-2 mb-4">
          {order.items.slice(0, 3).map((item, index) => (
            <div key={index} className="flex items-center space-x-3">
              {item.imageUrl && (
                <img
                  src={item.imageUrl}
                  alt={item.productName}
                  className="w-8 h-8 object-cover rounded"
                />
              )}
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-secondary-900 truncate">
                  {item.productName}
                </p>
                <p className="text-xs text-secondary-500">
                  Qty: {item.quantity} × ${item.price.toFixed(2)}
                </p>
              </div>
            </div>
          ))}
          
          {order.items.length > 3 && (
            <p className="text-sm text-secondary-500">
              +{order.items.length - 3} more item{order.items.length - 3 !== 1 ? 's' : ''}
            </p>
          )}
        </div>

        {/* Order Total */}
        <div className="flex items-center justify-between pt-4 border-t border-secondary-200">
          <div>
            <p className="text-lg font-semibold text-secondary-900">
              Total: ${order.totalAmount.toFixed(2)}
            </p>
            {order.trackingNumber && (
              <p className="text-sm text-secondary-500">
                Tracking: {order.trackingNumber}
              </p>
            )}
          </div>
          
          {showActions && (
            <div className="flex space-x-2">
              <Link to={`/orders/${order.id}`}>
                <Button variant="outline" size="sm">
                  View Details
                </Button>
              </Link>
              
              {order.status === OrderStatus.DELIVERED && (
                <Button size="sm">
                  Reorder
                </Button>
              )}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
