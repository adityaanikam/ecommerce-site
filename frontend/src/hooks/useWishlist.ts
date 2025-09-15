import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/contexts/AuthContext';
import { useNotifications } from '@/contexts/NotificationContext';
import { queryKeys, invalidateQueries } from '@/lib/react-query';
import { ApiError, getErrorMessage } from '@/lib/axios';
import { ProductDto } from '@/types/api';

interface WishlistItem {
  id: string;
  productId: string;
  product: ProductDto;
  addedAt: string;
  notes?: string;
}

interface Wishlist {
  id: string;
  name: string;
  description?: string;
  isPublic: boolean;
  isDefault: boolean;
  items: WishlistItem[];
  createdAt: string;
  updatedAt: string;
  shareToken?: string;
}

interface CreateWishlistRequest {
  name: string;
  description?: string;
  isPublic?: boolean;
}

interface ShareWishlistRequest {
  wishlistId: string;
  email?: string;
  message?: string;
  expiresAt?: string;
}

// Hook for user's wishlists
export const useWishlists = () => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: queryKeys.users.wishlist,
    queryFn: async (): Promise<Wishlist[]> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/wishlists`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch wishlists');
      }
      
      return response.json();
    },
    enabled: isAuthenticated,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for a specific wishlist
export const useWishlist = (wishlistId: string) => {
  return useQuery({
    queryKey: ['wishlist', wishlistId],
    queryFn: async (): Promise<Wishlist> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch wishlist');
      }
      
      return response.json();
    },
    enabled: !!wishlistId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for public wishlist (by share token)
export const usePublicWishlist = (shareToken: string) => {
  return useQuery({
    queryKey: ['wishlist', 'public', shareToken],
    queryFn: async (): Promise<Wishlist> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/public/${shareToken}`
      );
      
      if (!response.ok) {
        throw new Error('Failed to fetch public wishlist');
      }
      
      return response.json();
    },
    enabled: !!shareToken,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for wishlist management
export const useWishlistManagement = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Create wishlist
  const createWishlistMutation = useMutation({
    mutationFn: async (data: CreateWishlistRequest): Promise<Wishlist> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/users/wishlists`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to create wishlist');
      }
      
      return response.json();
    },
    onSuccess: () => {
      invalidateQueries.users();
      showSuccess('Wishlist created successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Update wishlist
  const updateWishlistMutation = useMutation({
    mutationFn: async ({ wishlistId, data }: { wishlistId: string; data: Partial<CreateWishlistRequest> }): Promise<Wishlist> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to update wishlist');
      }
      
      return response.json();
    },
    onSuccess: (_, { wishlistId }) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist', wishlistId] });
      invalidateQueries.users();
      showSuccess('Wishlist updated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Delete wishlist
  const deleteWishlistMutation = useMutation({
    mutationFn: async (wishlistId: string): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to delete wishlist');
      }
    },
    onSuccess: () => {
      invalidateQueries.users();
      showSuccess('Wishlist deleted successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    createWishlist: createWishlistMutation.mutateAsync,
    updateWishlist: updateWishlistMutation.mutateAsync,
    deleteWishlist: deleteWishlistMutation.mutateAsync,
    isCreating: createWishlistMutation.isPending,
    isUpdating: updateWishlistMutation.isPending,
    isDeleting: deleteWishlistMutation.isPending,
  };
};

// Hook for wishlist items management
export const useWishlistItems = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useNotifications();

  // Add item to wishlist
  const addToWishlistMutation = useMutation({
    mutationFn: async ({ wishlistId, productId, notes }: { wishlistId: string; productId: string; notes?: string }): Promise<WishlistItem> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}/items`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ productId, notes }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to add item to wishlist');
      }
      
      return response.json();
    },
    onSuccess: (_, { wishlistId }) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist', wishlistId] });
      invalidateQueries.users();
      showSuccess('Item added to wishlist');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Remove item from wishlist
  const removeFromWishlistMutation = useMutation({
    mutationFn: async ({ wishlistId, itemId }: { wishlistId: string; itemId: string }): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}/items/${itemId}`,
        {
          method: 'DELETE',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to remove item from wishlist');
      }
    },
    onSuccess: (_, { wishlistId }) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist', wishlistId] });
      invalidateQueries.users();
      showSuccess('Item removed from wishlist');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Move item between wishlists
  const moveItemMutation = useMutation({
    mutationFn: async ({ fromWishlistId, toWishlistId, itemId }: { fromWishlistId: string; toWishlistId: string; itemId: string }): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${fromWishlistId}/items/${itemId}/move`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ toWishlistId }),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to move item');
      }
    },
    onSuccess: (_, { fromWishlistId, toWishlistId }) => {
      queryClient.invalidateQueries({ queryKey: ['wishlist', fromWishlistId] });
      queryClient.invalidateQueries({ queryKey: ['wishlist', toWishlistId] });
      invalidateQueries.users();
      showSuccess('Item moved successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  return {
    addToWishlist: addToWishlistMutation.mutateAsync,
    removeFromWishlist: removeFromWishlistMutation.mutateAsync,
    moveItem: moveItemMutation.mutateAsync,
    isAdding: addToWishlistMutation.isPending,
    isRemoving: removeFromWishlistMutation.isPending,
    isMoving: moveItemMutation.isPending,
  };
};

// Hook for wishlist sharing
export const useWishlistSharing = () => {
  const { showSuccess, showError } = useNotifications();

  // Generate share link
  const generateShareLinkMutation = useMutation({
    mutationFn: async (wishlistId: string): Promise<{ shareToken: string; shareUrl: string }> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${wishlistId}/share`,
        {
          method: 'POST',
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to generate share link');
      }
      
      return response.json();
    },
    onSuccess: () => {
      showSuccess('Share link generated successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Share via email
  const shareViaEmailMutation = useMutation({
    mutationFn: async (data: ShareWishlistRequest): Promise<void> => {
      const response = await fetch(
        `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/wishlists/${data.wishlistId}/share/email`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        throw new Error('Failed to share wishlist');
      }
    },
    onSuccess: () => {
      showSuccess('Wishlist shared successfully');
    },
    onError: (error: ApiError) => {
      showError(getErrorMessage(error));
    },
  });

  // Copy to clipboard
  const copyToClipboard = async (text: string): Promise<void> => {
    try {
      await navigator.clipboard.writeText(text);
      showSuccess('Link copied to clipboard');
    } catch (error) {
      showError('Failed to copy link');
    }
  };

  return {
    generateShareLink: generateShareLinkMutation.mutateAsync,
    shareViaEmail: shareViaEmailMutation.mutateAsync,
    copyToClipboard,
    isGeneratingLink: generateShareLinkMutation.isPending,
    isSharing: shareViaEmailMutation.isPending,
  };
};

// Hook for quick wishlist operations
export const useQuickWishlist = () => {
  const { wishlists } = useWishlists();
  const { addToWishlist, removeFromWishlist } = useWishlistItems();
  const { isAuthenticated } = useAuth();

  const defaultWishlist = wishlists?.find(w => w.isDefault);

  const toggleWishlist = async (productId: string) => {
    if (!isAuthenticated || !defaultWishlist) return;

    const existingItem = defaultWishlist.items.find(item => item.productId === productId);
    
    if (existingItem) {
      await removeFromWishlist({ wishlistId: defaultWishlist.id, itemId: existingItem.id });
    } else {
      await addToWishlist({ wishlistId: defaultWishlist.id, productId });
    }
  };

  const isInWishlist = (productId: string): boolean => {
    if (!defaultWishlist) return false;
    return defaultWishlist.items.some(item => item.productId === productId);
  };

  return {
    toggleWishlist,
    isInWishlist,
    defaultWishlist,
  };
};

export default useWishlist;
