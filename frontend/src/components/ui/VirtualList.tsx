import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { cn } from '@/utils/cn';

interface VirtualListProps<T> {
  items: T[];
  itemHeight: number;
  containerHeight: number;
  renderItem: (item: T, index: number) => React.ReactNode;
  className?: string;
  overscan?: number;
  onScroll?: (scrollTop: number) => void;
  loading?: boolean;
  loadingComponent?: React.ReactNode;
  emptyComponent?: React.ReactNode;
  keyExtractor?: (item: T, index: number) => string | number;
}

export const VirtualList = <T,>({
  items,
  itemHeight,
  containerHeight,
  renderItem,
  className,
  overscan = 5,
  onScroll,
  loading = false,
  loadingComponent,
  emptyComponent,
  keyExtractor = (_, index) => index,
}: VirtualListProps<T>) => {
  const [scrollTop, setScrollTop] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const scrollElementRef = useRef<HTMLDivElement>(null);

  // Calculate visible range
  const visibleRange = useMemo(() => {
    const startIndex = Math.floor(scrollTop / itemHeight);
    const endIndex = Math.min(
      startIndex + Math.ceil(containerHeight / itemHeight) + overscan,
      items.length - 1
    );
    
    return {
      startIndex: Math.max(0, startIndex - overscan),
      endIndex,
    };
  }, [scrollTop, itemHeight, containerHeight, overscan, items.length]);

  // Get visible items
  const visibleItems = useMemo(() => {
    return items.slice(visibleRange.startIndex, visibleRange.endIndex + 1);
  }, [items, visibleRange]);

  // Calculate total height
  const totalHeight = items.length * itemHeight;

  // Calculate offset for visible items
  const offsetY = visibleRange.startIndex * itemHeight;

  // Handle scroll
  const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    const newScrollTop = e.currentTarget.scrollTop;
    setScrollTop(newScrollTop);
    onScroll?.(newScrollTop);
  }, [onScroll]);

  // Scroll to specific item
  const scrollToItem = useCallback((index: number) => {
    if (containerRef.current) {
      const targetScrollTop = index * itemHeight;
      containerRef.current.scrollTop = targetScrollTop;
    }
  }, [itemHeight]);

  // Scroll to top
  const scrollToTop = useCallback(() => {
    scrollToItem(0);
  }, [scrollToItem]);

  // Scroll to bottom
  const scrollToBottom = useCallback(() => {
    scrollToItem(items.length - 1);
  }, [scrollToItem, items.length]);

  // Auto-scroll to maintain position when items change
  useEffect(() => {
    if (containerRef.current && items.length > 0) {
      const currentScrollTop = containerRef.current.scrollTop;
      const maxScrollTop = totalHeight - containerHeight;
      
      if (currentScrollTop > maxScrollTop) {
        containerRef.current.scrollTop = maxScrollTop;
      }
    }
  }, [items.length, totalHeight, containerHeight]);

  if (loading) {
    return (
      <div className={cn('flex items-center justify-center', className)} style={{ height: containerHeight }}>
        {loadingComponent || <div className="text-secondary-600 dark:text-secondary-400">Loading...</div>}
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className={cn('flex items-center justify-center', className)} style={{ height: containerHeight }}>
        {emptyComponent || <div className="text-secondary-600 dark:text-secondary-400">No items found</div>}
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      className={cn('overflow-auto', className)}
      style={{ height: containerHeight }}
      onScroll={handleScroll}
    >
      <div
        ref={scrollElementRef}
        style={{ height: totalHeight, position: 'relative' }}
      >
        <div
          style={{
            transform: `translateY(${offsetY}px)`,
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
          }}
        >
          {visibleItems.map((item, index) => {
            const actualIndex = visibleRange.startIndex + index;
            return (
              <div
                key={keyExtractor(item, actualIndex)}
                style={{ height: itemHeight }}
                className="flex items-center"
              >
                {renderItem(item, actualIndex)}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

// Hook for virtual list state management
export const useVirtualList = <T,>(
  items: T[],
  itemHeight: number,
  containerHeight: number
) => {
  const [scrollTop, setScrollTop] = useState(0);
  const [isScrolling, setIsScrolling] = useState(false);
  const scrollTimeoutRef = useRef<NodeJS.Timeout>();

  const handleScroll = useCallback((newScrollTop: number) => {
    setScrollTop(newScrollTop);
    setIsScrolling(true);

    // Clear existing timeout
    if (scrollTimeoutRef.current) {
      clearTimeout(scrollTimeoutRef.current);
    }

    // Set timeout to mark scrolling as finished
    scrollTimeoutRef.current = setTimeout(() => {
      setIsScrolling(false);
    }, 150);

    return () => {
      if (scrollTimeoutRef.current) {
        clearTimeout(scrollTimeoutRef.current);
      }
    };
  }, []);

  const visibleRange = useMemo(() => {
    const startIndex = Math.floor(scrollTop / itemHeight);
    const endIndex = Math.min(
      startIndex + Math.ceil(containerHeight / itemHeight),
      items.length - 1
    );
    
    return {
      startIndex: Math.max(0, startIndex),
      endIndex,
    };
  }, [scrollTop, itemHeight, containerHeight, items.length]);

  return {
    scrollTop,
    isScrolling,
    visibleRange,
    handleScroll,
  };
};

// Infinite virtual list for large datasets
interface InfiniteVirtualListProps<T> extends Omit<VirtualListProps<T>, 'items'> {
  items: T[];
  hasMore: boolean;
  loadMore: () => void;
  threshold?: number;
}

export const InfiniteVirtualList = <T,>({
  items,
  hasMore,
  loadMore,
  threshold = 5,
  ...props
}: InfiniteVirtualListProps<T>) => {
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const handleScroll = useCallback((scrollTop: number) => {
    props.onScroll?.(scrollTop);

    if (!hasMore || isLoadingMore) return;

    const { itemHeight, containerHeight } = props;
    const visibleStartIndex = Math.floor(scrollTop / itemHeight);
    const visibleEndIndex = Math.min(
      visibleStartIndex + Math.ceil(containerHeight / itemHeight),
      items.length - 1
    );

    // Load more when approaching the end
    if (visibleEndIndex >= items.length - threshold) {
      setIsLoadingMore(true);
      loadMore();
    }
  }, [hasMore, isLoadingMore, items.length, loadMore, props, threshold]);

  useEffect(() => {
    if (isLoadingMore) {
      const timer = setTimeout(() => {
        setIsLoadingMore(false);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [isLoadingMore]);

  return (
    <VirtualList
      {...props}
      items={items}
      onScroll={handleScroll}
      loadingComponent={
        isLoadingMore ? (
          <div className="flex items-center justify-center p-4">
            <div className="text-secondary-600 dark:text-secondary-400">Loading more...</div>
          </div>
        ) : props.loadingComponent
      }
    />
  );
};

export default VirtualList;
