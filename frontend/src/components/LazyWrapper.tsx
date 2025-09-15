import React, { Suspense, lazy, ComponentType } from 'react';
import { LoadingSpinner } from '@/components/ui';

interface LazyWrapperProps {
  fallback?: React.ReactNode;
}

// Higher-order component for lazy loading
export const withLazyLoading = <P extends object>(
  Component: ComponentType<P>,
  fallback?: React.ReactNode
) => {
  const LazyComponent = lazy(() => Promise.resolve({ default: Component }));
  
  return (props: P) => (
    <Suspense fallback={fallback || <LoadingSpinner size="lg" />}>
      <LazyComponent {...props} />
    </Suspense>
  );
};

// Lazy wrapper component
export const LazyWrapper: React.FC<LazyWrapperProps & { children: React.ReactNode }> = ({
  children,
  fallback = <LoadingSpinner size="lg" />
}) => {
  return <Suspense fallback={fallback}>{children}</Suspense>;
};

// Preload function for critical components
export const preloadComponent = (importFn: () => Promise<any>) => {
  return () => {
    importFn();
    return importFn();
  };
};

// Lazy load with preloading
export const lazyWithPreload = <T extends ComponentType<any>>(
  importFn: () => Promise<{ default: T }>
) => {
  const Component = lazy(importFn);
  
  // Add preload method to the component
  (Component as any).preload = importFn;
  
  return Component;
};

export default LazyWrapper;
