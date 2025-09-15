import { useEffect, useRef, useCallback, useState } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';

interface UseInfiniteScrollOptions {
  threshold?: number;
  rootMargin?: string;
  enabled?: boolean;
}

interface InfiniteScrollResult<T> {
  data: T[] | undefined;
  isLoading: boolean;
  isError: boolean;
  error: Error | null;
  hasNextPage: boolean;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
  refetch: () => void;
  loadMoreRef: (node: HTMLElement | null) => void;
}

export const useInfiniteScroll = <T>(
  queryKey: any[],
  queryFn: ({ pageParam }: { pageParam: number }) => Promise<{
    data: T[];
    hasNextPage: boolean;
    currentPage: number;
  }>,
  options: UseInfiniteScrollOptions = {}
): InfiniteScrollResult<T> => {
  const {
    threshold = 0.1,
    rootMargin = '100px',
    enabled = true,
  } = options;

  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLElement | null>(null);

  const {
    data,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey,
    queryFn,
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Flatten all pages data
  const flattenedData = data?.pages.flatMap(page => page.data);

  // Intersection observer callback
  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [target] = entries;
      if (target.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [hasNextPage, isFetchingNextPage, fetchNextPage]
  );

  // Set up intersection observer
  useEffect(() => {
    if (!enabled || !hasNextPage) return;

    const observer = new IntersectionObserver(handleObserver, {
      threshold,
      rootMargin,
    });

    observerRef.current = observer;

    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [enabled, hasNextPage, handleObserver, threshold, rootMargin]);

  // Ref callback for the load more element
  const loadMoreRefCallback = useCallback((node: HTMLElement | null) => {
    if (loadMoreRef.current) {
      observerRef.current?.unobserve(loadMoreRef.current);
    }

    loadMoreRef.current = node;

    if (node && observerRef.current) {
      observerRef.current.observe(node);
    }
  }, []);

  return {
    data: flattenedData,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
    loadMoreRef: loadMoreRefCallback,
  };
};

// Hook for infinite scrolling with manual trigger
export const useInfiniteScrollManual = <T>(
  queryKey: any[],
  queryFn: ({ pageParam }: { pageParam: number }) => Promise<{
    data: T[];
    hasNextPage: boolean;
    currentPage: number;
  }>,
  options: { enabled?: boolean } = {}
) => {
  const { enabled = true } = options;

  const {
    data,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey,
    queryFn,
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Flatten all pages data
  const flattenedData = data?.pages.flatMap(page => page.data);

  return {
    data: flattenedData,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  };
};

// Hook for scroll-based infinite loading
export const useScrollInfinite = <T>(
  queryKey: any[],
  queryFn: ({ pageParam }: { pageParam: number }) => Promise<{
    data: T[];
    hasNextPage: boolean;
    currentPage: number;
  }>,
  options: {
    enabled?: boolean;
    threshold?: number;
    rootElement?: HTMLElement | null;
  } = {}
) => {
  const { enabled = true, threshold = 0.8, rootElement } = options;

  const {
    data,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  } = useInfiniteQuery({
    queryKey,
    queryFn,
    getNextPageParam: (lastPage) => {
      return lastPage.hasNextPage ? lastPage.currentPage + 1 : undefined;
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Flatten all pages data
  const flattenedData = data?.pages.flatMap(page => page.data);

  // Scroll event handler
  const handleScroll = useCallback(() => {
    if (!enabled || !hasNextPage || isFetchingNextPage) return;

    const element = rootElement || window;
    const scrollTop = rootElement ? rootElement.scrollTop : window.pageYOffset;
    const scrollHeight = rootElement ? rootElement.scrollHeight : document.documentElement.scrollHeight;
    const clientHeight = rootElement ? rootElement.clientHeight : window.innerHeight;

    const scrollPercentage = (scrollTop + clientHeight) / scrollHeight;

    if (scrollPercentage >= threshold) {
      fetchNextPage();
    }
  }, [enabled, hasNextPage, isFetchingNextPage, fetchNextPage, threshold, rootElement]);

  // Set up scroll listener
  useEffect(() => {
    if (!enabled) return;

    const element = rootElement || window;
    element.addEventListener('scroll', handleScroll, { passive: true });

    return () => {
      element.removeEventListener('scroll', handleScroll);
    };
  }, [enabled, handleScroll, rootElement]);

  return {
    data: flattenedData,
    isLoading,
    isError,
    error,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    refetch,
  };
};

// Hook for virtual scrolling (for large lists)
export const useVirtualScroll = <T>(
  items: T[],
  itemHeight: number,
  containerHeight: number,
  overscan: number = 5
) => {
  const [scrollTop, setScrollTop] = useState(0);

  const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - overscan);
  const endIndex = Math.min(
    items.length - 1,
    Math.ceil((scrollTop + containerHeight) / itemHeight) + overscan
  );

  const visibleItems = items.slice(startIndex, endIndex + 1);
  const totalHeight = items.length * itemHeight;
  const offsetY = startIndex * itemHeight;

  const handleScroll = useCallback((event: React.UIEvent<HTMLDivElement>) => {
    setScrollTop(event.currentTarget.scrollTop);
  }, []);

  return {
    visibleItems,
    totalHeight,
    offsetY,
    handleScroll,
    startIndex,
    endIndex,
  };
};

export default useInfiniteScroll;
