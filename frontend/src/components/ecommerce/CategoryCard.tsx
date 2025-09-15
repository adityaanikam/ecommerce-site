import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { Card, CardContent, Badge } from '@/components/ui';
import { cn } from '@/utils/cn';
import { Category } from '@/types';

export interface CategoryCardProps {
  category: Category;
  productCount?: number;
  className?: string;
  variant?: 'default' | 'featured' | 'minimal';
}

export const CategoryCard: React.FC<CategoryCardProps> = ({
  category,
  productCount,
  className,
  variant = 'default',
}) => {
  const [imageError, setImageError] = React.useState(false);

  if (variant === 'minimal') {
    return (
      <Link
        to={`/products?category=${category.id}`}
        className={cn(
          'block p-4 rounded-lg border border-secondary-200 hover:border-primary-300 hover:shadow-sm transition-all duration-200 group',
          className
        )}
      >
        <div className="flex items-center space-x-3">
          {category.imageUrl && !imageError ? (
            <img
              src={category.imageUrl}
              alt={category.name}
              className="w-10 h-10 object-cover rounded-lg"
              onError={() => setImageError(true)}
            />
          ) : (
            <div className="w-10 h-10 bg-secondary-100 rounded-lg flex items-center justify-center">
              <span className="text-secondary-400 text-sm font-medium">
                {category.name.charAt(0)}
              </span>
            </div>
          )}
          
          <div className="flex-1 min-w-0">
            <h3 className="font-medium text-secondary-900 truncate group-hover:text-primary-600 transition-colors">
              {category.name}
            </h3>
            {productCount !== undefined && (
              <p className="text-sm text-secondary-500">
                {productCount} products
              </p>
            )}
          </div>
          
          <ArrowRight className="h-4 w-4 text-secondary-400 group-hover:text-primary-600 transition-colors" />
        </div>
      </Link>
    );
  }

  if (variant === 'featured') {
    return (
      <Link
        to={`/products?category=${category.id}`}
        className={cn(
          'block group relative overflow-hidden rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 text-white hover:shadow-lg transition-all duration-300',
          className
        )}
      >
        <div className="p-8">
          <div className="relative z-10">
            <h3 className="text-2xl font-bold mb-2 group-hover:scale-105 transition-transform duration-200">
              {category.name}
            </h3>
            {category.description && (
              <p className="text-primary-100 mb-4 line-clamp-2">
                {category.description}
              </p>
            )}
            {productCount !== undefined && (
              <Badge variant="secondary" className="bg-white/20 text-white border-white/30">
                {productCount} products
              </Badge>
            )}
          </div>
          
          {category.imageUrl && !imageError && (
            <div className="absolute top-0 right-0 w-32 h-32 opacity-20">
              <img
                src={category.imageUrl}
                alt={category.name}
                className="w-full h-full object-cover"
                onError={() => setImageError(true)}
              />
            </div>
          )}
        </div>
      </Link>
    );
  }

  // Default variant
  return (
    <Link
      to={`/products?category=${category.id}`}
      className={cn(
        'block group hover:shadow-md transition-all duration-200',
        className
      )}
    >
      <Card className="overflow-hidden">
        <CardContent className="p-0">
          <div className="relative">
            {/* Image */}
            <div className="aspect-[4/3] bg-secondary-100 overflow-hidden">
              {category.imageUrl && !imageError ? (
                <img
                  src={category.imageUrl}
                  alt={category.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  onError={() => setImageError(true)}
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center">
                  <span className="text-4xl font-bold text-secondary-400">
                    {category.name.charAt(0)}
                  </span>
                </div>
              )}
            </div>

            {/* Content */}
            <div className="p-6">
              <h3 className="text-lg font-semibold text-secondary-900 mb-2 group-hover:text-primary-600 transition-colors">
                {category.name}
              </h3>
              
              {category.description && (
                <p className="text-sm text-secondary-600 mb-4 line-clamp-2">
                  {category.description}
                </p>
              )}

              <div className="flex items-center justify-between">
                {productCount !== undefined && (
                  <span className="text-sm text-secondary-500">
                    {productCount} products
                  </span>
                )}
                
                <ArrowRight className="h-4 w-4 text-secondary-400 group-hover:text-primary-600 transition-colors" />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
};
