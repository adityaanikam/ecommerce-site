import React from 'react';
import { cn } from '@/utils/cn';

export interface ContainerProps extends React.HTMLAttributes<HTMLDivElement> {
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const sizeClasses = {
  sm: 'max-w-3xl',
  md: 'max-w-5xl',
  lg: 'max-w-7xl',
  xl: 'max-w-8xl',
  full: 'max-w-full',
};

const paddingClasses = {
  none: '',
  sm: 'px-4',
  md: 'px-6',
  lg: 'px-8',
};

export const Container: React.FC<ContainerProps> = ({
  className,
  size = 'lg',
  padding = 'md',
  children,
  ...props
}) => {
  return (
    <div
      className={cn(
        'mx-auto w-full',
        sizeClasses[size],
        paddingClasses[padding],
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};
