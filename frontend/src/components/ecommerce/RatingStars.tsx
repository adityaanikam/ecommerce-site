import React from 'react';
import { Star, StarHalf } from 'lucide-react';
import { cn } from '@/utils/cn';

interface RatingStarsProps {
  rating: number;
  maxRating?: number;
  size?: 'sm' | 'md' | 'lg';
  interactive?: boolean;
  onChange?: (rating: number) => void;
  className?: string;
}

export const RatingStars: React.FC<RatingStarsProps> = ({
  rating,
  maxRating = 5,
  size = 'md',
  interactive = false,
  onChange,
  className,
}) => {
  const [hoverRating, setHoverRating] = React.useState<number | null>(null);

  const sizes = {
    sm: 'h-3 w-3',
    md: 'h-4 w-4',
    lg: 'h-5 w-5',
  };

  const renderStar = (index: number) => {
    const value = index + 1;
    const isHovered = interactive && hoverRating !== null && value <= hoverRating;
    const isActive = (hoverRating !== null ? hoverRating : rating) >= value;
    const isHalf = !isHovered && !isActive && rating > index && rating < value;

    return (
      <button
        key={index}
        className={cn(
          'focus:outline-none transition-colors',
          interactive && 'cursor-pointer hover:scale-110 transition-transform',
          !interactive && 'cursor-default'
        )}
        onClick={() => interactive && onChange?.(value)}
        onMouseEnter={() => interactive && setHoverRating(value)}
        onMouseLeave={() => interactive && setHoverRating(null)}
        disabled={!interactive}
      >
        {isHalf ? (
          <StarHalf
            className={cn(
              sizes[size],
              'text-warning-500 fill-warning-500'
            )}
          />
        ) : (
          <Star
            className={cn(
              sizes[size],
              isActive
                ? 'text-warning-500 fill-warning-500'
                : 'text-secondary-300 dark:text-secondary-600'
            )}
          />
        )}
      </button>
    );
  };

  return (
    <div
      className={cn(
        'flex items-center gap-0.5',
        className
      )}
    >
      {Array.from({ length: maxRating }, (_, i) => renderStar(i))}
    </div>
  );
};