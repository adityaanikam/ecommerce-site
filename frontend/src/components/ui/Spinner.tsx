import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/utils/cn';

const spinnerVariants = cva(
  'animate-spin rounded-full border-solid',
  {
    variants: {
      size: {
        sm: 'h-4 w-4 border-2',
        md: 'h-6 w-6 border-2',
        lg: 'h-8 w-8 border-2',
        xl: 'h-12 w-12 border-4',
      },
      variant: {
        default: 'border-primary-600 border-t-transparent',
        secondary: 'border-secondary-600 border-t-transparent',
        success: 'border-success-600 border-t-transparent',
        warning: 'border-warning-600 border-t-transparent',
        error: 'border-error-600 border-t-transparent',
        white: 'border-white border-t-transparent',
      },
    },
    defaultVariants: {
      size: 'md',
      variant: 'default',
    },
  }
);

export interface SpinnerProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof spinnerVariants> {
  text?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({
  className,
  size,
  variant,
  text,
  ...props
}) => {
  return (
    <div className="flex flex-col items-center justify-center">
      <div
        className={cn(spinnerVariants({ size, variant }), className)}
        {...props}
      />
      {text && (
        <p className="mt-2 text-sm text-secondary-600 dark:text-secondary-400">
          {text}
        </p>
      )}
    </div>
  );
};

// Dots spinner variant
export const DotsSpinner: React.FC<{ className?: string }> = ({ className }) => {
  return (
    <div className={cn('flex space-x-1', className)}>
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          className="h-2 w-2 bg-primary-600 rounded-full animate-pulse"
          style={{
            animationDelay: `${i * 0.2}s`,
            animationDuration: '1s',
          }}
        />
      ))}
    </div>
  );
};

// Pulse spinner variant
export const PulseSpinner: React.FC<{ className?: string }> = ({ className }) => {
  return (
    <div
      className={cn(
        'h-8 w-8 bg-primary-600 rounded-full animate-pulse',
        className
      )}
    />
  );
};
