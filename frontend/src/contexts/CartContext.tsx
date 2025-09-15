import React, { createContext, useContext, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CartService, AddToCartRequest, UpdateCartItemRequest } from '@/services/api';
import { CartDto, CartItemDto } from '@/types/api';
import { queryKeys, invalidateQueries, optimisticUpdates } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';

interface CartContextType {
  cart: CartDto | null;
  isLoading: boolean;
  error: string | null;
  addToCart: (data: AddToCartRequest) => Promise<void>;
  updateCartItem: (data: UpdateCartItemRequest) => Promise<void>;
  removeFromCart: (itemId: string) => Promise<void>;
  clearCart: () => Promise<void>;
  moveToWishlist: (itemId: string) => Promise<void>;
  applyCoupon: (couponCode: string) => Promise<void>;
  removeCoupon: () => Promise<void>;
  setShippingOption: (shippingOptionId: string) => Promise<void>;
  validateCart: () => Promise<void>;
  totalItems: number;
  totalPrice: number;
  isEligibleForFreeShipping: boolean;
  clearError: () => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = useQueryClient();

  // Get cart query
  const {
    data: cart,
    isLoading,
    error: cartError,
  } = useQuery({
    queryKey: queryKeys.cart.current,
    queryFn: async () => {
      const response = await CartService.getCart();
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: false,
  });

  // Add to cart mutation
  const addToCartMutation = useMutation({
    mutationFn: CartService.addToCart,
    onMutate: async (newItem: AddToCartRequest) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: queryKeys.cart.current });

      // Snapshot previous value
      const previousCart = queryClient.getQueryData<CartDto>(queryKeys.cart.current);

      // Optimistically update
      if (previousCart) {
        const existingItem = previousCart.items.find(
          item => item.productId === newItem.productId && 
          JSON.stringify(item.variant) === JSON.stringify(newItem.variant)
        );

        if (existingItem) {
          // Update existing item quantity
          optimisticUpdates.updateCartItemQuantity(existingItem.id, existingItem.quantity + newItem.quantity);
        } else {
          // Add new item (we'll create a temporary item)
          const tempItem: CartItemDto = {
            id: `temp-${Date.now()}`,
            productId: newItem.productId,
            quantity: newItem.quantity,
            variant: newItem.variant,
            price: 0, // Will be updated when real data comes back
            subtotal: 0,
            product: {
              id: newItem.productId,
              name: 'Loading...',
              price: 0,
              images: [],
              brand: '',
              inStock: true,
            },
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          };
          optimisticUpdates.addToCart(tempItem);
        }
      }

      return { previousCart };
    },
    onError: (error: ApiError, newItem, context) => {
      // Rollback on error
      if (context?.previousCart) {
        queryClient.setQueryData(queryKeys.cart.current, context.previousCart);
      }
    },
    onSuccess: () => {
      // Invalidate and refetch cart data
      invalidateQueries.cart();
    },
  });

  // Update cart item mutation
  const updateCartItemMutation = useMutation({
    mutationFn: CartService.updateCartItem,
    onMutate: async (data: UpdateCartItemRequest) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.cart.current });
      const previousCart = queryClient.getQueryData<CartDto>(queryKeys.cart.current);
      
      // Optimistically update
      optimisticUpdates.updateCartItemQuantity(data.itemId, data.quantity);
      
      return { previousCart };
    },
    onError: (error: ApiError, data, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(queryKeys.cart.current, context.previousCart);
      }
    },
    onSuccess: () => {
      invalidateQueries.cart();
    },
  });

  // Remove from cart mutation
  const removeFromCartMutation = useMutation({
    mutationFn: CartService.removeFromCart,
    onMutate: async (itemId: string) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.cart.current });
      const previousCart = queryClient.getQueryData<CartDto>(queryKeys.cart.current);
      
      // Optimistically remove
      optimisticUpdates.removeFromCart(itemId);
      
      return { previousCart };
    },
    onError: (error: ApiError, itemId, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(queryKeys.cart.current, context.previousCart);
      }
    },
    onSuccess: () => {
      invalidateQueries.cart();
    },
  });

  // Clear cart mutation
  const clearCartMutation = useMutation({
    mutationFn: CartService.clearCart,
    onSuccess: () => {
      queryClient.setQueryData(queryKeys.cart.current, null);
    },
  });

  // Move to wishlist mutation
  const moveToWishlistMutation = useMutation({
    mutationFn: CartService.moveToWishlist,
    onSuccess: () => {
      invalidateQueries.cart();
      invalidateQueries.users(); // Invalidate wishlist
    },
  });

  // Apply coupon mutation
  const applyCouponMutation = useMutation({
    mutationFn: CartService.applyCoupon,
    onSuccess: () => {
      invalidateQueries.cart();
    },
  });

  // Remove coupon mutation
  const removeCouponMutation = useMutation({
    mutationFn: CartService.removeCoupon,
    onSuccess: () => {
      invalidateQueries.cart();
    },
  });

  // Set shipping option mutation
  const setShippingOptionMutation = useMutation({
    mutationFn: CartService.setShippingOption,
    onSuccess: () => {
      invalidateQueries.cart();
    },
  });

  // Validate cart mutation
  const validateCartMutation = useMutation({
    mutationFn: CartService.validateCart,
    onSuccess: (response) => {
      // Handle validation results
      const validationResult = response.data;
      if (!validationResult.isValid) {
        console.warn('Cart validation failed:', validationResult.errors);
      }
    },
  });

  // Action functions
  const addToCart = useCallback(async (data: AddToCartRequest) => {
    await addToCartMutation.mutateAsync(data);
  }, [addToCartMutation]);

  const updateCartItem = useCallback(async (data: UpdateCartItemRequest) => {
    await updateCartItemMutation.mutateAsync(data);
  }, [updateCartItemMutation]);

  const removeFromCart = useCallback(async (itemId: string) => {
    await removeFromCartMutation.mutateAsync(itemId);
  }, [removeFromCartMutation]);

  const clearCart = useCallback(async () => {
    await clearCartMutation.mutateAsync();
  }, [clearCartMutation]);

  const moveToWishlist = useCallback(async (itemId: string) => {
    await moveToWishlistMutation.mutateAsync(itemId);
  }, [moveToWishlistMutation]);

  const applyCoupon = useCallback(async (couponCode: string) => {
    await applyCouponMutation.mutateAsync(couponCode);
  }, [applyCouponMutation]);

  const removeCoupon = useCallback(async () => {
    await removeCouponMutation.mutateAsync();
  }, [removeCouponMutation]);

  const setShippingOption = useCallback(async (shippingOptionId: string) => {
    await setShippingOptionMutation.mutateAsync(shippingOptionId);
  }, [setShippingOptionMutation]);

  const validateCart = useCallback(async () => {
    await validateCartMutation.mutateAsync();
  }, [validateCartMutation]);

  const clearError = useCallback(() => {
    // Clear any cart-related errors
    queryClient.setQueryData(queryKeys.cart.current, (oldData: any) => {
      if (oldData) {
        return { ...oldData, error: null };
      }
      return oldData;
    });
  }, [queryClient]);

  // Computed values
  const totalItems = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;
  const totalPrice = cart?.items.reduce((sum, item) => sum + item.subtotal, 0) || 0;
  const isEligibleForFreeShipping = totalPrice >= 50; // Assuming $50 threshold

  const error = cartError ? getErrorMessage(cartError) : null;

  const value: CartContextType = {
    cart,
    isLoading,
    error,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    moveToWishlist,
    applyCoupon,
    removeCoupon,
    setShippingOption,
    validateCart,
    totalItems,
    totalPrice,
    isEligibleForFreeShipping,
    clearError,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};