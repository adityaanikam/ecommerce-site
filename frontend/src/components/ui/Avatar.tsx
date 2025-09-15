import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/utils/cn';
import { User } from 'lucide-react';

const avatarVariants = cva(
  'relative flex shrink-0 overflow-hidden rounded-full',
  {
    variants: {
      size: {
        sm: 'h-8 w-8',
        md: 'h-10 w-10',
        lg: 'h-12 w-12',
        xl: 'h-16 w-16',
        '2xl': 'h-20 w-20',
      },
    },
    defaultVariants: {
      size: 'md',
    },
  }
);

export interface AvatarProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof avatarVariants> {
  src?: string;
  alt?: string;
  fallback?: string;
  status?: 'online' | 'offline' | 'away' | 'busy';
}

export const Avatar: React.FC<AvatarProps> = ({
  className,
  size,
  src,
  alt,
  fallback,
  status,
  ...props
}) => {
  const [imageError, setImageError] = React.useState(false);

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const statusColors = {
    online: 'bg-success-500',
    offline: 'bg-secondary-400',
    away: 'bg-warning-500',
    busy: 'bg-error-500',
  };

  const statusSizes = {
    sm: 'h-2 w-2',
    md: 'h-2.5 w-2.5',
    lg: 'h-3 w-3',
    xl: 'h-4 w-4',
    '2xl': 'h-5 w-5',
  };

  return (
    <div className={cn(avatarVariants({ size }), className)} {...props}>
      {src && !imageError ? (
        <img
          src={src}
          alt={alt}
          className="h-full w-full object-cover"
          onError={() => setImageError(true)}
        />
      ) : (
        <div className="flex h-full w-full items-center justify-center bg-secondary-100 text-secondary-600 dark:bg-secondary-800 dark:text-secondary-400">
          {fallback ? (
            <span className="text-sm font-medium">
              {getInitials(fallback)}
            </span>
          ) : (
            <User className="h-4 w-4" />
          )}
        </div>
      )}
      
      {status && (
        <div
          className={cn(
            'absolute bottom-0 right-0 rounded-full border-2 border-white dark:border-secondary-900',
            statusColors[status],
            statusSizes[size || 'md']
          )}
        />
      )}
    </div>
  );
};
