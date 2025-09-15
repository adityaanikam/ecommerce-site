import React, { useState, useRef, useEffect, useCallback } from 'react';
import { cn } from '@/utils/cn';

interface OptimizedImageProps {
  src: string;
  alt: string;
  width?: number;
  height?: number;
  className?: string;
  placeholder?: string;
  blurDataURL?: string;
  priority?: boolean;
  quality?: number;
  sizes?: string;
  loading?: 'lazy' | 'eager';
  onLoad?: () => void;
  onError?: () => void;
  fallback?: React.ReactNode;
}

export const OptimizedImage: React.FC<OptimizedImageProps> = ({
  src,
  alt,
  width,
  height,
  className,
  placeholder,
  blurDataURL,
  priority = false,
  quality = 75,
  sizes = '100vw',
  loading = 'lazy',
  onLoad,
  onError,
  fallback,
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isInView, setIsInView] = useState(priority);
  const [hasError, setHasError] = useState(false);
  const [currentSrc, setCurrentSrc] = useState<string | null>(null);
  const imgRef = useRef<HTMLImageElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);

  // Generate optimized image URL
  const generateOptimizedUrl = useCallback((originalSrc: string, width?: number, quality?: number) => {
    // If using a CDN or image optimization service, modify this function
    // For now, we'll use the original src
    if (width && quality) {
      // Example for Cloudinary or similar service:
      // return `https://res.cloudinary.com/your-cloud/image/fetch/w_${width},q_${quality},f_auto/${encodeURIComponent(originalSrc)}`;
      
      // For now, return original src
      return originalSrc;
    }
    return originalSrc;
  }, []);

  // Intersection Observer for lazy loading
  useEffect(() => {
    if (priority || isInView) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setIsInView(true);
            observer.unobserve(entry.target);
          }
        });
      },
      {
        rootMargin: '50px', // Start loading 50px before the image comes into view
        threshold: 0.1,
      }
    );

    observerRef.current = observer;

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [priority, isInView]);

  // Load image when in view
  useEffect(() => {
    if (isInView && !currentSrc) {
      const optimizedSrc = generateOptimizedUrl(src, width, quality);
      setCurrentSrc(optimizedSrc);
    }
  }, [isInView, src, width, quality, currentSrc, generateOptimizedUrl]);

  const handleLoad = useCallback(() => {
    setIsLoaded(true);
    onLoad?.();
  }, [onLoad]);

  const handleError = useCallback(() => {
    setHasError(true);
    onError?.();
  }, [onError]);

  // Preload critical images
  useEffect(() => {
    if (priority && src) {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'image';
      link.href = generateOptimizedUrl(src, width, quality);
      document.head.appendChild(link);

      return () => {
        document.head.removeChild(link);
      };
    }
  }, [priority, src, width, quality, generateOptimizedUrl]);

  if (hasError && fallback) {
    return <>{fallback}</>;
  }

  return (
    <div
      ref={imgRef}
      className={cn('relative overflow-hidden', className)}
      style={{ width, height }}
    >
      {/* Placeholder/Blur */}
      {!isLoaded && (placeholder || blurDataURL) && (
        <div
          className="absolute inset-0 bg-secondary-200 dark:bg-secondary-700 animate-pulse"
          style={{
            backgroundImage: blurDataURL ? `url(${blurDataURL})` : undefined,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            filter: blurDataURL ? 'blur(10px)' : undefined,
          }}
        />
      )}

      {/* Loading Skeleton */}
      {!isLoaded && !placeholder && !blurDataURL && (
        <div className="absolute inset-0 bg-secondary-200 dark:bg-secondary-700 animate-pulse" />
      )}

      {/* Actual Image */}
      {currentSrc && (
        <img
          src={currentSrc}
          alt={alt}
          width={width}
          height={height}
          loading={loading}
          sizes={sizes}
          className={cn(
            'transition-opacity duration-300',
            isLoaded ? 'opacity-100' : 'opacity-0',
            'w-full h-full object-cover'
          )}
          onLoad={handleLoad}
          onError={handleError}
          decoding="async"
        />
      )}

      {/* Error State */}
      {hasError && !fallback && (
        <div className="absolute inset-0 flex items-center justify-center bg-secondary-100 dark:bg-secondary-800">
          <div className="text-center p-4">
            <div className="w-8 h-8 bg-secondary-300 dark:bg-secondary-600 rounded-full flex items-center justify-center mx-auto mb-2">
              <span className="text-sm text-secondary-600 dark:text-secondary-400">📷</span>
            </div>
            <p className="text-xs text-secondary-600 dark:text-secondary-400">Failed to load</p>
          </div>
        </div>
      )}
    </div>
  );
};

// Hook for image preloading
export const useImagePreload = () => {
  const preloadImage = useCallback((src: string): Promise<void> => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve();
      img.onerror = reject;
      img.src = src;
    });
  }, []);

  const preloadImages = useCallback(async (srcs: string[]): Promise<void[]> => {
    return Promise.all(srcs.map(preloadImage));
  }, [preloadImage]);

  return { preloadImage, preloadImages };
};

// Image optimization utilities
export const imageUtils = {
  // Generate responsive image sizes
  generateSizes: (breakpoints: Record<string, number>): string => {
    const sizeQueries = Object.entries(breakpoints)
      .map(([breakpoint, size]) => `(max-width: ${breakpoint}px) ${size}px`)
      .join(', ');
    return `${sizeQueries}, 100vw`;
  },

  // Generate srcset for responsive images
  generateSrcSet: (baseSrc: string, widths: number[]): string => {
    return widths
      .map(width => `${baseSrc}?w=${width} ${width}w`)
      .join(', ');
  },

  // Get optimal image format
  getOptimalFormat: (): string => {
    if (typeof window !== 'undefined') {
      const canvas = document.createElement('canvas');
      const webpSupported = canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0;
      const avifSupported = canvas.toDataURL('image/avif').indexOf('data:image/avif') === 0;
      
      if (avifSupported) return 'avif';
      if (webpSupported) return 'webp';
    }
    return 'jpeg';
  },

  // Generate blur data URL
  generateBlurDataURL: (width: number = 10, height: number = 10): string => {
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.fillStyle = '#f3f4f6';
      ctx.fillRect(0, 0, width, height);
    }
    return canvas.toDataURL();
  },
};

export default OptimizedImage;
