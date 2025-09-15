import React, { useState, useRef, useEffect } from 'react';
import { ChevronLeft, ChevronRight, ZoomIn, ZoomOut, RotateCw, Download, Share2, Heart } from 'lucide-react';
import { Button } from '@/components/ui';
import { cn } from '@/utils/cn';

interface ProductGalleryProps {
  images: string[];
  productName: string;
  onImageClick?: (imageUrl: string, index: number) => void;
  className?: string;
}

export const ProductGallery: React.FC<ProductGalleryProps> = ({
  images,
  productName,
  onImageClick,
  className,
}) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isZoomed, setIsZoomed] = useState(false);
  const [zoomLevel, setZoomLevel] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [imageLoaded, setImageLoaded] = useState<boolean[]>(new Array(images.length).fill(false));
  
  const mainImageRef = useRef<HTMLImageElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const currentImage = images[currentIndex];

  // Handle image load
  const handleImageLoad = (index: number) => {
    setImageLoaded(prev => {
      const newState = [...prev];
      newState[index] = true;
      return newState;
    });
  };

  // Navigation functions
  const goToPrevious = () => {
    setCurrentIndex(prev => (prev === 0 ? images.length - 1 : prev - 1));
    resetZoom();
  };

  const goToNext = () => {
    setCurrentIndex(prev => (prev === images.length - 1 ? 0 : prev + 1));
    resetZoom();
  };

  const goToImage = (index: number) => {
    setCurrentIndex(index);
    resetZoom();
  };

  // Zoom functions
  const resetZoom = () => {
    setIsZoomed(false);
    setZoomLevel(1);
    setRotation(0);
  };

  const toggleZoom = () => {
    if (isZoomed) {
      setIsZoomed(false);
      setZoomLevel(1);
    } else {
      setIsZoomed(true);
      setZoomLevel(2);
    }
  };

  const zoomIn = () => {
    setZoomLevel(prev => Math.min(prev + 0.5, 5));
    setIsZoomed(true);
  };

  const zoomOut = () => {
    setZoomLevel(prev => {
      const newLevel = Math.max(prev - 0.5, 0.5);
      if (newLevel === 0.5) {
        setIsZoomed(false);
        return 1;
      }
      return newLevel;
    });
  };

  const rotateImage = () => {
    setRotation(prev => (prev + 90) % 360);
  };

  // Fullscreen functions
  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      containerRef.current?.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (isFullscreen) {
        switch (e.key) {
          case 'ArrowLeft':
            goToPrevious();
            break;
          case 'ArrowRight':
            goToNext();
            break;
          case 'Escape':
            document.exitFullscreen();
            setIsFullscreen(false);
            break;
          case '+':
          case '=':
            zoomIn();
            break;
          case '-':
            zoomOut();
            break;
          case 'r':
          case 'R':
            rotateImage();
            break;
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isFullscreen]);

  // Mouse wheel zoom
  const handleWheel = (e: React.WheelEvent) => {
    if (e.ctrlKey || e.metaKey) {
      e.preventDefault();
      if (e.deltaY < 0) {
        zoomIn();
      } else {
        zoomOut();
      }
    }
  };

  // Download image
  const downloadImage = async () => {
    try {
      const response = await fetch(currentImage);
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${productName}-${currentIndex + 1}.jpg`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to download image:', error);
    }
  };

  // Share image
  const shareImage = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: productName,
          text: `Check out this product: ${productName}`,
          url: window.location.href,
        });
      } catch (error) {
        console.error('Error sharing:', error);
      }
    } else {
      // Fallback: copy URL to clipboard
      navigator.clipboard.writeText(window.location.href);
    }
  };

  if (!images || images.length === 0) {
    return (
      <div className={cn('flex items-center justify-center bg-secondary-100 dark:bg-secondary-800 rounded-lg', className)}>
        <div className="text-center p-8">
          <div className="w-16 h-16 bg-secondary-200 dark:bg-secondary-700 rounded-lg flex items-center justify-center mx-auto mb-4">
            <span className="text-2xl text-secondary-400">📷</span>
          </div>
          <p className="text-secondary-600 dark:text-secondary-400">No images available</p>
        </div>
      </div>
    );
  }

  return (
    <div className={cn('relative', className)}>
      {/* Main Image Container */}
      <div
        ref={containerRef}
        className="relative bg-white dark:bg-secondary-800 rounded-lg overflow-hidden group"
        onWheel={handleWheel}
      >
        {/* Main Image */}
        <div className="relative aspect-square overflow-hidden">
          <img
            ref={mainImageRef}
            src={currentImage}
            alt={`${productName} - Image ${currentIndex + 1}`}
            className={cn(
              'w-full h-full object-cover transition-all duration-300 cursor-zoom-in',
              isZoomed && 'cursor-zoom-out',
              !imageLoaded[currentIndex] && 'opacity-0'
            )}
            style={{
              transform: `scale(${zoomLevel}) rotate(${rotation}deg)`,
              transformOrigin: 'center center',
            }}
            onClick={() => onImageClick?.(currentImage, currentIndex)}
            onLoad={() => handleImageLoad(currentIndex)}
            onError={() => handleImageLoad(currentIndex)}
          />
          
          {/* Loading Skeleton */}
          {!imageLoaded[currentIndex] && (
            <div className="absolute inset-0 bg-secondary-200 dark:bg-secondary-700 animate-pulse" />
          )}
        </div>

        {/* Navigation Arrows */}
        {images.length > 1 && (
          <>
            <Button
              variant="outline"
              size="icon"
              className="absolute left-2 top-1/2 transform -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-white/90 dark:bg-secondary-800/90"
              onClick={goToPrevious}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="absolute right-2 top-1/2 transform -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-white/90 dark:bg-secondary-800/90"
              onClick={goToNext}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </>
        )}

        {/* Image Counter */}
        {images.length > 1 && (
          <div className="absolute top-2 right-2 bg-black/50 text-white px-2 py-1 rounded text-sm">
            {currentIndex + 1} / {images.length}
          </div>
        )}

        {/* Zoom Controls */}
        <div className="absolute bottom-2 left-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <Button
            variant="outline"
            size="icon-sm"
            onClick={zoomOut}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <ZoomOut className="h-3 w-3" />
          </Button>
          <Button
            variant="outline"
            size="icon-sm"
            onClick={toggleZoom}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <ZoomIn className="h-3 w-3" />
          </Button>
          <Button
            variant="outline"
            size="icon-sm"
            onClick={zoomIn}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <ZoomIn className="h-3 w-3" />
          </Button>
        </div>

        {/* Action Controls */}
        <div className="absolute bottom-2 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <Button
            variant="outline"
            size="icon-sm"
            onClick={rotateImage}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <RotateCw className="h-3 w-3" />
          </Button>
          <Button
            variant="outline"
            size="icon-sm"
            onClick={downloadImage}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <Download className="h-3 w-3" />
          </Button>
          <Button
            variant="outline"
            size="icon-sm"
            onClick={shareImage}
            className="bg-white/90 dark:bg-secondary-800/90"
          >
            <Share2 className="h-3 w-3" />
          </Button>
        </div>

        {/* Fullscreen Toggle */}
        <Button
          variant="outline"
          size="icon-sm"
          className="absolute top-2 left-2 opacity-0 group-hover:opacity-100 transition-opacity bg-white/90 dark:bg-secondary-800/90"
          onClick={toggleFullscreen}
        >
          <span className="text-xs">⛶</span>
        </Button>
      </div>

      {/* Thumbnail Navigation */}
      {images.length > 1 && (
        <div className="mt-4 flex gap-2 overflow-x-auto pb-2">
          {images.map((image, index) => (
            <button
              key={index}
              onClick={() => goToImage(index)}
              className={cn(
                'flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition-all',
                index === currentIndex
                  ? 'border-primary-500 ring-2 ring-primary-200 dark:ring-primary-800'
                  : 'border-secondary-200 dark:border-secondary-700 hover:border-secondary-300 dark:hover:border-secondary-600'
              )}
            >
              <img
                src={image}
                alt={`${productName} thumbnail ${index + 1}`}
                className="w-full h-full object-cover"
              />
            </button>
          ))}
        </div>
      )}

      {/* Zoom Level Indicator */}
      {isZoomed && (
        <div className="absolute top-2 left-1/2 transform -translate-x-1/2 bg-black/50 text-white px-2 py-1 rounded text-sm">
          {Math.round(zoomLevel * 100)}%
        </div>
      )}

      {/* Keyboard Shortcuts Help */}
      {isFullscreen && (
        <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 bg-black/50 text-white px-4 py-2 rounded text-sm">
          <div className="flex gap-4 text-xs">
            <span>← → Navigate</span>
            <span>+ - Zoom</span>
            <span>R Rotate</span>
            <span>ESC Exit</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductGallery;
