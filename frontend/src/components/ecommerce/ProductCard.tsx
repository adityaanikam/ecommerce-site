import React from 'react';
import { motion } from 'framer-motion';
import { Heart, Eye, ShoppingCart, Plus, Minus } from 'lucide-react';
import { Button, Card, CardContent, Badge } from '@/components';
import { Product } from '@/types/api';
import { cn } from '@/utils/cn';
import { AddToCartAnimation } from './AddToCartAnimation';

interface ProductCardProps {
  product: Product;
  variant?: 'default' | 'compact';
  className?: string;
  onAddToCart?: (id: string, quantity: number) => void;
  onAddToWishlist?: (id: string) => void;
  onQuickView?: (id: string) => void;
  onViewDetails?: (id: string) => void;
  isInWishlist?: (id: string) => boolean;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  variant = 'default',
  className,
  onAddToCart,
  onAddToWishlist,
  onQuickView,
  onViewDetails,
  isInWishlist,
}) => {
  const [isHovered, setIsHovered] = React.useState(false);
  const [quantity, setQuantity] = React.useState(1);
  const [isAddingToCart, setIsAddingToCart] = React.useState(false);
  const [currentImageIndex, setCurrentImageIndex] = React.useState(0);

  const handleQuantityChange = (delta: number) => {
    const newQuantity = Math.max(1, Math.min(quantity + delta, product.stock));
    setQuantity(newQuantity);
  };

  const handleAddToCart = () => {
    if (onAddToCart) {
      setIsAddingToCart(true);
      onAddToCart(product.id, quantity);
    }
  };

  const handleAnimationComplete = () => {
    setIsAddingToCart(false);
  };

  // Auto-rotate product images on hover
  React.useEffect(() => {
    let interval: NodeJS.Timeout;
    if (isHovered && product.images.length > 1) {
      interval = setInterval(() => {
        setCurrentImageIndex((prev) => (prev + 1) % product.images.length);
      }, 2000);
    }
    return () => clearInterval(interval);
  }, [isHovered, product.images.length]);

  const hasDiscount = product.discountPrice !== undefined;
  const discountPercentage = hasDiscount
    ? Math.round(((product.price - product.discountPrice!) / product.price) * 100)
    : 0;

  return (
    <>
      <Card
        className={cn(
          'group hover-elevate overflow-hidden backdrop-blur-sm border border-transparent',
          'hover:border-primary-100/50 dark:hover:border-primary-400/30 transition-all duration-300',
          'dark:bg-secondary-800 dark:border-secondary-700',
          className
        )}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        <CardContent className="p-0">
          <div className="relative">
            {/* Image with overlay gradient */}
            <div className="aspect-square bg-secondary-100 dark:bg-secondary-700 rounded-t-lg overflow-hidden overlay-gradient-bottom">
              <motion.img
                key={currentImageIndex}
                src={product.images[currentImageIndex]}
                alt={product.name}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ease-out"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.3 }}
              />

              {/* Badges */}
              <div className="absolute top-3 left-3 flex flex-col space-y-2 z-10">
                {hasDiscount && (
                  <Badge variant="error" className="backdrop-blur-md bg-error-500/80 shadow-md border border-error-400/30 text-xs font-bold">
                    {discountPercentage}% off
                  </Badge>
                )}
                {product.stock === 0 && (
                  <Badge variant="secondary" className="backdrop-blur-md bg-secondary-700/70 shadow-md border border-secondary-600/30 text-xs font-bold">
                    Out of Stock
                  </Badge>
                )}
                {product.stock > 0 && product.stock < 10 && (
                  <Badge variant="warning" className="backdrop-blur-md bg-warning-500/80 shadow-md border border-warning-400/30 text-xs font-bold">
                    Only {product.stock} left
                  </Badge>
                )}
              </div>

              {/* Quick Actions */}
              <div
                className={cn(
                  'absolute top-3 right-3 flex flex-col space-y-2 transition-all duration-300 z-10',
                  'transform',
                  isHovered ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-4'
                )}
              >
                <Button
                  size="icon-sm"
                  variant="secondary"
                  onClick={() => onAddToWishlist?.(product.id)}
                  className={cn(
                    "bg-glass hover:bg-white dark:hover:bg-secondary-700 shadow-md backdrop-blur-md hover:shadow-lg transition-all duration-300",
                    isInWishlist?.(product.id) && "bg-primary-100 text-primary-600 dark:bg-primary-900 dark:text-primary-400"
                  )}
                >
                  <Heart className={cn("h-4 w-4", isInWishlist?.(product.id) && "fill-current")} />
                </Button>
                <Button
                  size="icon-sm"
                  variant="secondary"
                  onClick={() => onQuickView?.(product.id)}
                  className="bg-glass hover:bg-white dark:hover:bg-secondary-700 shadow-md backdrop-blur-md hover:shadow-lg transition-all duration-300"
                >
                  <Eye className="h-4 w-4" />
                </Button>
              </div>

              {/* Image Navigation Dots */}
              {product.images.length > 1 && (
                <div className="absolute bottom-3 left-1/2 transform -translate-x-1/2 flex space-x-1">
                  {product.images.map((_, index) => (
                    <button
                      key={index}
                      className={cn(
                        'w-1.5 h-1.5 rounded-full transition-all duration-300',
                        currentImageIndex === index
                          ? 'bg-white'
                          : 'bg-white/50 hover:bg-white/75'
                      )}
                      onClick={() => setCurrentImageIndex(index)}
                    />
                  ))}
                </div>
              )}
            </div>

            {/* Content */}
            <div className="p-5 bg-gradient-to-b from-white to-secondary-50/30 dark:from-secondary-800 dark:to-secondary-900/80">
              <div className="mb-3">
                <h3 className="font-semibold text-secondary-900 dark:text-white line-clamp-2 mb-1.5 text-[1.05rem] tracking-tight">
                  {product.name}
                </h3>
                <p className="text-sm text-secondary-500 dark:text-secondary-400 font-medium">
                  {product.brand}
                </p>
                <p className="text-sm text-secondary-600 dark:text-secondary-300 mt-2 line-clamp-2 leading-relaxed">
                  {product.description}
                </p>
              </div>

              {/* Price */}
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-2">
                  <span className="text-xl font-bold text-primary-600 dark:text-primary-400">
                    ${(product.discountPrice || product.price).toFixed(2)}
                  </span>
                  {hasDiscount && (
                    <span className="text-sm text-secondary-500 dark:text-secondary-400 line-through opacity-75">
                      ${product.price.toFixed(2)}
                    </span>
                  )}
                </div>
              </div>

              {/* Quantity Controls */}
              {product.stock > 0 && (
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="outline"
                      size="icon-sm"
                      onClick={() => handleQuantityChange(-1)}
                      disabled={quantity <= 1}
                    >
                      <Minus className="h-4 w-4" />
                    </Button>
                    <span className="w-8 text-center font-medium">
                      {quantity}
                    </span>
                    <Button
                      variant="outline"
                      size="icon-sm"
                      onClick={() => handleQuantityChange(1)}
                      disabled={quantity >= product.stock}
                    >
                      <Plus className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              )}

              {/* Actions */}
              <div className="space-y-2">
                <Button
                  className="w-full bg-primary-600 hover:bg-primary-700 dark:bg-primary-500 dark:hover:bg-primary-600 text-white shadow-md hover:shadow-lg transform transition-all duration-300 active:scale-[0.98]"
                  disabled={product.stock === 0}
                  onClick={handleAddToCart}
                >
                  <ShoppingCart className="mr-2 h-4 w-4" />
                  Add to Cart
                </Button>
                <Button
                  variant="secondary"
                  className="w-full bg-white hover:bg-secondary-50 dark:bg-secondary-800 dark:hover:bg-secondary-700 border border-secondary-200 dark:border-secondary-600 shadow-sm hover:shadow-md transform transition-all duration-300 active:scale-[0.98]"
                  onClick={() => onViewDetails?.(product.id)}
                >
                  View Details
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <AddToCartAnimation
        isAnimating={isAddingToCart}
        onAnimationComplete={handleAnimationComplete}
      />
    </>
  );
};