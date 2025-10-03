import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronLeft, ChevronRight, ZoomIn } from 'lucide-react';
import { Button } from '@/components';
import { cn } from '@/utils/cn';

interface ProductGalleryProps {
  images: string[];
  alt: string;
  className?: string;
}

export const ProductGallery: React.FC<ProductGalleryProps> = ({
  images,
  alt,
  className,
}) => {
  const [selectedIndex, setSelectedIndex] = React.useState(0);
  const [isZoomed, setIsZoomed] = React.useState(false);
  const [mousePosition, setMousePosition] = React.useState({ x: 0, y: 0 });
  const containerRef = React.useRef<HTMLDivElement>(null);

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;

    const { left, top, width, height } = containerRef.current.getBoundingClientRect();
    const x = ((e.clientX - left) / width) * 100;
    const y = ((e.clientY - top) / height) * 100;

    setMousePosition({ x, y });
  };

  const nextImage = () => {
    setSelectedIndex((prev) => (prev + 1) % images.length);
  };

  const previousImage = () => {
    setSelectedIndex((prev) => (prev - 1 + images.length) % images.length);
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* Main Image Container */}
      <div
        ref={containerRef}
        className="relative aspect-square bg-white dark:bg-secondary-800 rounded-lg overflow-hidden"
        onMouseMove={handleMouseMove}
        onMouseEnter={() => setIsZoomed(true)}
        onMouseLeave={() => setIsZoomed(false)}
      >
        <AnimatePresence mode="wait">
          <motion.div
            key={selectedIndex}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.3 }}
            className="absolute inset-0"
          >
            <div
              className={cn(
                'w-full h-full transition-transform duration-200',
                isZoomed ? 'scale-150' : 'scale-100'
              )}
              style={{
                transformOrigin: `${mousePosition.x}% ${mousePosition.y}%`,
              }}
            >
              <img
                src={images[selectedIndex]}
                alt={`${alt} ${selectedIndex + 1}`}
                className="w-full h-full object-cover"
              />
            </div>
          </motion.div>
        </AnimatePresence>

        {/* Navigation Buttons */}
        {images.length > 1 && (
          <>
            <Button
              variant="secondary"
              size="icon"
              onClick={previousImage}
              className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white/80 dark:bg-secondary-800/80 backdrop-blur-sm hover:bg-white dark:hover:bg-secondary-800"
            >
              <ChevronLeft className="h-5 w-5" />
            </Button>
            <Button
              variant="secondary"
              size="icon"
              onClick={nextImage}
              className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white/80 dark:bg-secondary-800/80 backdrop-blur-sm hover:bg-white dark:hover:bg-secondary-800"
            >
              <ChevronRight className="h-5 w-5" />
            </Button>
          </>
        )}

        {/* Zoom Indicator */}
        <div className="absolute bottom-4 right-4 text-white bg-black/50 backdrop-blur-sm rounded-full p-2">
          <ZoomIn className="h-5 w-5" />
        </div>
      </div>

      {/* Thumbnails */}
      {images.length > 1 && (
        <div className="grid grid-cols-4 gap-4">
          {images.map((image, index) => (
            <button
              key={index}
              onClick={() => setSelectedIndex(index)}
              className={cn(
                'aspect-square rounded-lg overflow-hidden border-2 transition-colors',
                selectedIndex === index
                  ? 'border-primary-600'
                  : 'border-transparent hover:border-primary-400'
              )}
            >
              <img
                src={image}
                alt={`${alt} thumbnail ${index + 1}`}
                className="w-full h-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
};