import React from 'react';
import { Star } from 'lucide-react';
import { cn } from '@/utils/cn';

export interface RatingStarsProps {
  rating: number;
  maxRating?: number;
  size?: 'sm' | 'md' | 'lg';
  showNumber?: boolean;
  showCount?: boolean;
  count?: number;
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
  className?: string;
}

const sizeClasses = {
  sm: 'h-3 w-3',
  md: 'h-4 w-4',
  lg: 'h-5 w-5',
};

const textSizeClasses = {
  sm: 'text-xs',
  md: 'text-sm',
  lg: 'text-base',
};

export const RatingStars: React.FC<RatingStarsProps> = ({
  rating,
  maxRating = 5,
  size = 'md',
  showNumber = false,
  showCount = false,
  count,
  interactive = false,
  onRatingChange,
  className,
}) => {
  const [hoveredRating, setHoveredRating] = React.useState(0);
  const [isHovering, setIsHovering] = React.useState(false);

  const displayRating = isHovering ? hoveredRating : rating;

  const handleStarClick = (starRating: number) => {
    if (interactive && onRatingChange) {
      onRatingChange(starRating);
    }
  };

  const handleStarHover = (starRating: number) => {
    if (interactive) {
      setHoveredRating(starRating);
      setIsHovering(true);
    }
  };

  const handleMouseLeave = () => {
    if (interactive) {
      setIsHovering(false);
      setHoveredRating(0);
    }
  };

  return (
    <div className={cn('flex items-center space-x-1', className)}>
      <div 
        className="flex items-center"
        onMouseLeave={handleMouseLeave}
      >
        {Array.from({ length: maxRating }, (_, index) => {
          const starRating = index + 1;
          const isFilled = starRating <= Math.floor(displayRating);
          const isHalfFilled = starRating === Math.ceil(displayRating) && displayRating % 1 !== 0;

          return (
            <button
              key={index}
              type="button"
              className={cn(
                'transition-colors duration-150',
                interactive && 'cursor-pointer hover:scale-110',
                !interactive && 'cursor-default'
              )}
              onClick={() => handleStarClick(starRating)}
              onMouseEnter={() => handleStarHover(starRating)}
              disabled={!interactive}
            >
              <Star
                className={cn(
                  sizeClasses[size],
                  isFilled && 'fill-yellow-400 text-yellow-400',
                  isHalfFilled && 'fill-yellow-400/50 text-yellow-400',
                  !isFilled && !isHalfFilled && 'text-secondary-300',
                  interactive && 'hover:text-yellow-400'
                )}
              />
            </button>
          );
        })}
      </div>

      {/* Rating Number */}
      {showNumber && (
        <span className={cn(
          'text-secondary-600 font-medium ml-1',
          textSizeClasses[size]
        )}>
          {rating.toFixed(1)}
        </span>
      )}

      {/* Review Count */}
      {showCount && count !== undefined && (
        <span className={cn(
          'text-secondary-500 ml-1',
          textSizeClasses[size]
        )}>
          ({count})
        </span>
      )}
    </div>
  );
};
