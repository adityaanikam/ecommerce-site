import { useEffect, useRef, useState, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/lib/react-query';

interface WebSocketMessage {
  type: string;
  data: any;
  timestamp: string;
}

interface UseWebSocketOptions {
  url: string;
  enabled?: boolean;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
  onMessage?: (message: WebSocketMessage) => void;
  onError?: (error: Event) => void;
  onOpen?: () => void;
  onClose?: () => void;
}

interface UseWebSocketResult {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  sendMessage: (message: any) => void;
  reconnect: () => void;
  disconnect: () => void;
}

export const useWebSocket = (options: UseWebSocketOptions): UseWebSocketResult => {
  const {
    url,
    enabled = true,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    onMessage,
    onError,
    onOpen,
    onClose,
  } = options;

  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reconnectAttempts, setReconnectAttempts] = useState(0);

  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const queryClient = useQueryClient();

  const connect = useCallback(() => {
    if (!enabled || wsRef.current?.readyState === WebSocket.OPEN) {
      return;
    }

    setIsConnecting(true);
    setError(null);

    try {
      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        setIsConnected(true);
        setIsConnecting(false);
        setReconnectAttempts(0);
        setError(null);
        onOpen?.();
      };

      ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          
          // Handle different message types
          switch (message.type) {
            case 'CART_UPDATED':
              queryClient.invalidateQueries({ queryKey: queryKeys.cart.all });
              break;
            case 'ORDER_UPDATED':
              queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
              break;
            case 'PRODUCT_UPDATED':
              queryClient.invalidateQueries({ queryKey: queryKeys.products.all });
              break;
            case 'NOTIFICATION':
              queryClient.invalidateQueries({ queryKey: queryKeys.users.notifications });
              break;
            default:
              break;
          }

          onMessage?.(message);
        } catch (err) {
          console.error('Failed to parse WebSocket message:', err instanceof Error ? err.message : String(err));
        }
      };

      ws.onerror = (event) => {
        setIsConnected(false);
        setIsConnecting(false);
        setError('WebSocket connection error');
        onError?.(event);
      };

      ws.onclose = (event) => {
        setIsConnected(false);
        setIsConnecting(false);
        onClose?.();

        // Attempt to reconnect if not a clean close
        if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
          reconnectTimeoutRef.current = setTimeout(() => {
            setReconnectAttempts(prev => prev + 1);
            connect();
          }, reconnectInterval);
        }
      };
    } catch (err) {
      setIsConnecting(false);
      setError('Failed to create WebSocket connection');
    }
  }, [enabled, url, reconnectAttempts, maxReconnectAttempts, reconnectInterval, onMessage, onError, onOpen, onClose, queryClient]);

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (wsRef.current) {
      wsRef.current.close(1000, 'Manual disconnect');
      wsRef.current = null;
    }

    setIsConnected(false);
    setIsConnecting(false);
    setReconnectAttempts(0);
  }, []);

  const sendMessage = useCallback((message: any) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not connected. Cannot send message.');
    }
  }, []);

  const reconnect = useCallback(() => {
    disconnect();
    setReconnectAttempts(0);
    connect();
  }, [disconnect, connect]);

  // Connect on mount and when enabled changes
  useEffect(() => {
    if (enabled) {
      connect();
    } else {
      disconnect();
    }

    return () => {
      disconnect();
    };
  }, [enabled, connect, disconnect]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    isConnected,
    isConnecting,
    error,
    sendMessage,
    reconnect,
    disconnect,
  };
};

// Hook for real-time cart updates
export const useCartWebSocket = () => {
  const token = localStorage.getItem('accessToken');
  const wsUrl = token 
    ? `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}/cart?token=${token}`
    : null;

  return useWebSocket({
    url: wsUrl || '',
    enabled: !!wsUrl,
    onMessage: (message) => {
      if (message.type === 'CART_UPDATED') {
        console.log('Cart updated via WebSocket:', message.data);
      }
    },
  });
};

// Hook for real-time order updates
export const useOrderWebSocket = () => {
  const token = localStorage.getItem('accessToken');
  const wsUrl = token 
    ? `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}/orders?token=${token}`
    : null;

  return useWebSocket({
    url: wsUrl || '',
    enabled: !!wsUrl,
    onMessage: (message) => {
      if (message.type === 'ORDER_UPDATED') {
        console.log('Order updated via WebSocket:', message.data);
      }
    },
  });
};

// Hook for real-time notifications
export const useNotificationWebSocket = () => {
  const token = localStorage.getItem('accessToken');
  const wsUrl = token 
    ? `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}/notifications?token=${token}`
    : null;

  return useWebSocket({
    url: wsUrl || '',
    enabled: !!wsUrl,
    onMessage: (message) => {
      if (message.type === 'NOTIFICATION') {
        console.log('New notification via WebSocket:', message.data);
      }
    },
  });
};

// Hook for real-time product updates (admin)
export const useProductWebSocket = () => {
  const token = localStorage.getItem('accessToken');
  const wsUrl = token 
    ? `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}/products?token=${token}`
    : null;

  return useWebSocket({
    url: wsUrl || '',
    enabled: !!wsUrl,
    onMessage: (message) => {
      if (message.type === 'PRODUCT_UPDATED') {
        console.log('Product updated via WebSocket:', message.data);
      }
    },
  });
};

export default useWebSocket;
