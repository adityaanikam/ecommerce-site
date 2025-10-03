import React from 'react';
import { cn } from '@/utils/cn';

interface ContainerProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  size?: 'default' | 'sm' | 'lg' | 'xl' | 'full';
}

export const Container: React.FC<ContainerProps> = ({
  children,
  size = 'default',
  className,
  ...props
}) => {
  return (
    <div
      className={cn(
        'mx-auto px-4 sm:px-6 lg:px-8',
        {
          'max-w-7xl': size === 'default',
          'max-w-5xl': size === 'sm',
          'max-w-[1400px]': size === 'lg',
          'max-w-[1600px]': size === 'xl',
          'max-w-none': size === 'full',
        },
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};
