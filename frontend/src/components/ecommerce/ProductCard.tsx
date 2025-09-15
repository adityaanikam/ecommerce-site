import React from 'react';
import { Link } from 'react-router-dom';
import { Heart, ShoppingCart, Eye, Star } from 'lucide-react';
import { Card, CardContent, Badge, Button } from '@/components/ui';
import { cn } from '@/utils/cn';
import { Product } from '@/types';

export interface ProductCardProps {
  product: Product;
  onAddToCart?: (productId: string) => void;
  onAddToWishlist?: (productId: string) => void;
  onQuickView?: (productId: string) => void;
  onViewDetails?: (productId: string) => void;
  className?: string;
  showActions?: boolean;
  variant?: 'default' | 'compact' | 'detailed';
  isLoading?: boolean;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onAddToCart,
  onAddToWishlist,
  onQuickView,
  onViewDetails,
  className,
  showActions = true,
  variant = 'default',
  isLoading = false,
}) => {
  const [isHovered, setIsHovered] = React.useState(false);
  const [imageError, setImageError] = React.useState(false);

  const hasDiscount = product.discountPrice && product.discountPrice < product.price;
  const discountPercentage = hasDiscount 
    ? Math.round(((product.price - product.discountPrice!) / product.price) * 100)
    : 0;

  // Normalize rating fields to support different shapes used in tests
  const ratingAverage: number | undefined =
    (product as any).ratings?.average ?? (product as any).ratings?.averageRating ?? (product as any).averageRating;
  const ratingCount: number | undefined =
    (product as any).ratings?.count ?? (product as any).ratings?.totalRatings ?? (product as any).totalRatings;

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        className={cn(
          'h-4 w-4',
          i < Math.floor(rating) 
            ? 'fill-yellow-400 text-yellow-400' 
            : 'text-secondary-300'
        )}
      />
    ));
  };

  if (variant === 'compact') {
    return (
      <Card className={cn('group hover:shadow-md transition-all duration-200', className)}>
        <CardContent className="p-4">
          <div className="flex space-x-4">
            <div className="relative w-20 h-20 flex-shrink-0">
              {product.images && product.images.length > 0 && !imageError ? (
                <img
                  src={product.images[0]}
                  alt={product.name}
                  className="w-full h-full object-cover rounded-lg"
                  onError={() => setImageError(true)}
                />
              ) : (
                <div className="w-full h-full bg-secondary-100 rounded-lg flex items-center justify-center">
                  <span className="text-secondary-400 text-xs">No Image</span>
                </div>
              )}
            </div>
            
            <div className="flex-1 min-w-0">
              <h3 className="font-medium text-secondary-900 truncate">
                {product.name}
              </h3>
              <p className="text-sm text-secondary-500 truncate">
                {product.brand}
              </p>
              <div className="flex items-center justify-between mt-2">
                <div className="flex items-center space-x-2">
                  <span className="font-semibold text-primary-600">
                    ${product.discountPrice || product.price}
                  </span>
                  {hasDiscount && (
                    <span className="text-sm text-secondary-500 line-through">
                      ${product.price}
                    </span>
                  )}
                </div>
                {product.ratings && (
                  <div className="flex items-center space-x-1">
                    <Star className="h-3 w-3 fill-yellow-400 text-yellow-400" />
                    <span className="text-xs text-secondary-600">
                      {product.ratings.average.toFixed(1)}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (variant === 'detailed') {
    return (
      <Card className={cn('group hover:shadow-lg transition-all duration-300', className)}>
        <CardContent className="p-0">
          <div className="relative">
            {/* Image */}
            <div className="aspect-square bg-secondary-100 rounded-t-lg overflow-hidden">
              {product.images && product.images.length > 0 && !imageError ? (
                <img
                  src={product.images[0]}
                  alt={product.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  onError={() => setImageError(true)}
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <span className="text-secondary-400">No Image</span>
                </div>
              )}
            
              {/* Badges */}
              <div className="absolute top-3 left-3 flex flex-col space-y-2">
                {hasDiscount && (
                  <Badge variant="error" className="text-xs">
                    -{discountPercentage}%
                  </Badge>
                )}
                {product.stock === 0 && (
                  <Badge variant="secondary" className="text-xs">
                    Out of Stock
                  </Badge>
                )}
              </div>

              {/* Quick Actions */}
              {showActions && (
                <div className="absolute top-3 right-3 flex flex-col space-y-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                  <Button
                    size="icon-sm"
                    variant="secondary"
                    onClick={() => onAddToWishlist?.(product.id)}
                    className="bg-white/90 hover:bg-white shadow-md"
                  >
                    <Heart className="h-4 w-4" />
                  </Button>
                  <Button
                    size="icon-sm"
                    variant="secondary"
                    aria-label="View Details"
                    onClick={() => (onViewDetails ? onViewDetails(product.id) : onQuickView?.(product.id))}
                    className="bg-white/90 hover:bg-white shadow-md"
                  >
                    <Eye className="h-4 w-4" />
                  </Button>
                </div>
              )}
            </div>

            {/* Content */}
            <div className="p-6">
              <div className="mb-2">
                <h3 className="font-semibold text-secondary-900 line-clamp-2 mb-1">
                  {product.name}
                </h3>
                <p className="text-sm text-secondary-500">
                  {product.brand}
                </p>
              </div>

              <p className="text-sm text-secondary-600 line-clamp-2 mb-4">
                {product.description}
              </p>

              {/* Rating */}
              {product.ratings && (
                <div className="flex items-center space-x-2 mb-4">
                  <div className="flex items-center">
                    {renderStars(product.ratings.average)}
                  </div>
                  <span className="text-sm text-secondary-600">
                    ({product.ratings.count})
                  </span>
                </div>
              )}

              {/* Price */}
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-2">
                  <span className="text-xl font-bold text-primary-600">
                    ${product.discountPrice || product.price}
                  </span>
                  {hasDiscount && (
                    <span className="text-sm text-secondary-500 line-through">
                      ${product.price}
                    </span>
                  )}
                </div>
                <span className="text-sm text-secondary-500">
                  {product.stock > 0 ? `${product.stock} left` : 'Out of stock'}
                </span>
              </div>

              {/* Actions */}
              {showActions && (
                <div className="flex space-x-2">
                  <Button
                    className="flex-1"
                    disabled={isLoading || product.stock === 0}
                    onClick={() => onAddToCart?.(product.id)}
                  >
                    <ShoppingCart className="mr-2 h-4 w-4" />
                    Add to Cart
                  </Button>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Default variant
  return (
    <Card 
      className={cn('group hover:shadow-md transition-all duration-200', className)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <CardContent className="p-0">
        <div className="relative">
          {/* Image */}
          <div className="aspect-square bg-secondary-100 rounded-t-lg overflow-hidden">
            {product.images && product.images.length > 0 && !imageError ? (
              <img
                src={product.images[0]}
                alt={product.name}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                onError={() => setImageError(true)}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center">
                <span className="text-secondary-400">No Image</span>
              </div>
            )}
          
            {/* Badges */}
            <div className="absolute top-3 left-3 flex flex-col space-y-2">
              {hasDiscount && (
                <Badge variant="error" className="text-xs">
                  {discountPercentage}% off
                </Badge>
              )}
              {product.stock === 0 && (
                <Badge variant="secondary" className="text-xs">
                  Out of Stock
                </Badge>
              )}
            </div>

            {/* Quick Actions */}
            {showActions && (
              <div className={cn(
                'absolute top-3 right-3 flex flex-col space-y-2 transition-opacity duration-200',
                isHovered ? 'opacity-100' : 'opacity-0'
              )}>
                <Button
                  size="icon-sm"
                  variant="secondary"
                  onClick={() => onAddToWishlist?.(product.id)}
                  className="bg-white/90 hover:bg-white shadow-md"
                >
                  <Heart className="h-4 w-4" />
                </Button>
                <Button
                  size="icon-sm"
                  variant="secondary"
                  onClick={() => onQuickView?.(product.id)}
                  className="bg-white/90 hover:bg-white shadow-md"
                >
                  <Eye className="h-4 w-4" />
                </Button>
              </div>
            )}
          </div>

          {/* Content */}
          <div className="p-4">
            <div className="mb-2">
              <h3 className="font-semibold text-secondary-900 line-clamp-2 mb-1">
                {product.name}
              </h3>
              <p className="text-sm text-secondary-500">
                {product.brand}
              </p>
              {product.description && (
                <p className="text-sm text-secondary-600 mt-1 line-clamp-2">
                  {product.description}
                </p>
              )}
            </div>

            {/* Rating */}
            {(ratingAverage !== undefined || ratingCount !== undefined) && (
              <div className="flex items-center space-x-2 mb-3">
                <div className="flex items-center">
                  {renderStars(ratingAverage || 0)}
                </div>
                {ratingAverage !== undefined && (
                  <span className="text-sm text-secondary-700">{Number(ratingAverage).toFixed(1)}</span>
                )}
                <span className="text-sm text-secondary-600">({ratingCount ?? ''})</span>
              </div>
            )}

            {/* Price */}
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold text-primary-600">
                  ${product.discountPrice || product.price}
                </span>
                {hasDiscount && (
                  <span className="text-sm text-secondary-500 line-through">
                    ${product.price}
                  </span>
                )}
              </div>
            </div>

            {/* Actions */}
            {showActions && (
              <div className="space-y-2">
                <Button
                  className="w-full"
                  disabled={isLoading || product.stock === 0}
                  onClick={() => onAddToCart?.(product.id)}
                >
                  <ShoppingCart className="mr-2 h-4 w-4" />
                  Add to Cart
                </Button>
                <Button
                  variant="secondary"
                  className="w-full"
                  onClick={() => onViewDetails?.(product.id)}
                >
                  View Details
                </Button>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
