import React from 'react';
import { ArrowRight } from 'lucide-react';
import { Card, CardContent, Badge } from '@/components/ui';
import { cn } from '@/utils/cn';

export interface CategoryCardProps {
  name: string;
  description?: string;
  image?: string;
  productCount?: number;
  onClick?: () => void;
  className?: string;
  variant?: 'default' | 'featured' | 'minimal';
}

export const CategoryCard: React.FC<CategoryCardProps> = ({
  name,
  description,
  image,
  productCount,
  onClick,
  className,
  variant = 'default',
}) => {
  const [imageError, setImageError] = React.useState(false);

  if (variant === 'minimal') {
    return (
      <button
        onClick={onClick}
        className={cn(
          'block w-full p-4 rounded-lg border border-secondary-200 hover:border-primary-300 hover:shadow-sm transition-all duration-200 group',
          className
        )}
      >
        <div className="flex items-center space-x-3">
          {image && !imageError ? (
            <img
              src={image}
              alt={name}
              className="w-10 h-10 object-cover rounded-lg"
              onError={() => setImageError(true)}
            />
          ) : (
            <div className="w-10 h-10 bg-secondary-100 rounded-lg flex items-center justify-center">
              <span className="text-secondary-400 text-sm font-medium">
                {name.charAt(0)}
              </span>
            </div>
          )}
          
          <div className="flex-1 min-w-0">
            <h3 className="font-medium text-secondary-900 truncate group-hover:text-primary-600 transition-colors">
              {name}
            </h3>
            {productCount !== undefined && (
              <p className="text-sm text-secondary-500">
                {productCount} products
              </p>
            )}
          </div>
          
          <ArrowRight className="h-4 w-4 text-secondary-400 group-hover:text-primary-600 transition-colors" />
        </div>
      </button>
    );
  }

  if (variant === 'featured') {
    return (
      <button
        onClick={onClick}
        className={cn(
          'block w-full group relative overflow-hidden rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 text-white hover:shadow-lg transition-all duration-300',
          className
        )}
      >
        <div className="p-8">
          <div className="relative z-10">
            <h3 className="text-2xl font-bold mb-2 group-hover:scale-105 transition-transform duration-200">
              {name}
            </h3>
            {description && (
              <p className="text-primary-100 mb-4 line-clamp-2">
                {description}
              </p>
            )}
            {productCount !== undefined && (
              <Badge variant="secondary" className="bg-white/20 text-white border-white/30">
                {productCount} products
              </Badge>
            )}
          </div>
          
          {image && !imageError && (
            <div className="absolute top-0 right-0 w-32 h-32 opacity-20">
              <img
                src={image}
                alt={name}
                className="w-full h-full object-cover"
                onError={() => setImageError(true)}
              />
            </div>
          )}
        </div>
      </button>
    );
  }

  // Default variant with enhanced visual effects
  return (
    <button
      onClick={onClick}
      className={cn(
        'block w-full group hover-elevate rounded-xl overflow-hidden transition-all duration-300',
        className
      )}
    >
      <Card className="overflow-hidden border border-transparent hover:border-primary-100/30 bg-white dark:bg-secondary-800">
        <CardContent className="p-0">
          <div className="relative">
            {/* Image with overlay gradient */}
            <div className="aspect-[4/3] bg-secondary-100 overflow-hidden relative overlay-gradient-bottom">
              {image && !imageError ? (
                <img
                  src={image}
                  alt={name}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out"
                  onError={() => setImageError(true)}
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-secondary-200 to-secondary-300">
                  <span className="text-4xl font-bold text-white drop-shadow-md">
                    {name.charAt(0)}
                  </span>
                </div>
              )}
              
              {/* Overlay text for improved readability */}
              <div className="absolute inset-0 bg-gradient-to-t from-secondary-900/80 via-secondary-900/20 to-transparent opacity-0 group-hover:opacity-70 transition-opacity duration-300">
                <div className="absolute bottom-4 left-4 right-4 text-white">
                  <span className="inline-block px-3 py-1 bg-primary-600/80 backdrop-blur-sm text-xs font-semibold rounded-full">
                    {productCount || 0}+ products
                  </span>
                </div>
              </div>
            </div>

            {/* Content with improved typography and effects */}
            <div className="p-6 bg-gradient-to-b from-white to-secondary-50/30 dark:from-secondary-800 dark:to-secondary-700/30">
              <h3 className="text-lg font-semibold text-secondary-900 dark:text-white mb-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors tracking-tight">
                {name}
              </h3>
              
              {description && (
                <p className="text-sm text-secondary-600 dark:text-secondary-300 mb-4 line-clamp-2 leading-relaxed">
                  {description}
                </p>
              )}

              <div className="flex items-center justify-between mt-2">
                <div className="flex items-center">
                  {productCount !== undefined && (
                    <span className="text-sm text-secondary-500 dark:text-secondary-400 font-medium">
                      {productCount} products
                    </span>
                  )}
                </div>
                
                <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-primary-50 dark:bg-primary-900/30 group-hover:bg-primary-100 dark:group-hover:bg-primary-800/50 transition-colors">
                  <ArrowRight className="h-4 w-4 text-primary-600 dark:text-primary-400 group-hover:translate-x-0.5 transition-transform duration-300" />
                </span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </button>
  );
};