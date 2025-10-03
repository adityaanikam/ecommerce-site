import React from 'react';
import { Product } from '@/types/api';

interface WishlistState {
  items: Product[];
}

type WishlistAction =
  | { type: 'ADD_TO_WISHLIST'; payload: Product }
  | { type: 'REMOVE_FROM_WISHLIST'; payload: string }
  | { type: 'CLEAR_WISHLIST' }
  | { type: 'LOAD_WISHLIST'; payload: WishlistState };

import { STORAGE_KEYS } from '@/config';
const WISHLIST_STORAGE_KEY = STORAGE_KEYS.WISHLIST;

const initialState: WishlistState = {
  items: [],
};

const WishlistContext = React.createContext<{
  state: WishlistState;
  addToWishlist: (product: Product) => void;
  removeFromWishlist: (productId: string) => void;
  clearWishlist: () => void;
  isInWishlist: (productId: string) => boolean;
} | null>(null);

const wishlistReducer = (state: WishlistState, action: WishlistAction): WishlistState => {
  switch (action.type) {
    case 'ADD_TO_WISHLIST': {
      const exists = state.items.some(item => item.id === action.payload.id);
      if (exists) return state;
      return {
        ...state,
        items: [...state.items, action.payload],
      };
    }

    case 'REMOVE_FROM_WISHLIST': {
      return {
        ...state,
        items: state.items.filter(item => item.id !== action.payload),
      };
    }

    case 'CLEAR_WISHLIST':
      return initialState;

    case 'LOAD_WISHLIST':
      return action.payload;

    default:
      return state;
  }
};

export const WishlistProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = React.useReducer(wishlistReducer, initialState);

  // Load wishlist from localStorage on mount
  React.useEffect(() => {
    const savedWishlist = localStorage.getItem(WISHLIST_STORAGE_KEY);
    if (savedWishlist) {
      try {
        const parsedWishlist = JSON.parse(savedWishlist);
        dispatch({ type: 'LOAD_WISHLIST', payload: parsedWishlist });
      } catch (error) {
        console.error('Failed to parse wishlist from localStorage:', error instanceof Error ? error.message : String(error));
      }
    }
  }, []);

  // Save wishlist to localStorage whenever it changes
  React.useEffect(() => {
    localStorage.setItem(WISHLIST_STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  const addToWishlist = (product: Product) => {
    dispatch({ type: 'ADD_TO_WISHLIST', payload: product });
  };

  const removeFromWishlist = (productId: string) => {
    dispatch({ type: 'REMOVE_FROM_WISHLIST', payload: productId });
  };

  const clearWishlist = () => {
    dispatch({ type: 'CLEAR_WISHLIST' });
  };

  const isInWishlist = (productId: string) => {
    return state.items.some(item => item.id === productId);
  };

  return (
    <WishlistContext.Provider
      value={{
        state,
        addToWishlist,
        removeFromWishlist,
        clearWishlist,
        isInWishlist,
      }}
    >
      {children}
    </WishlistContext.Provider>
  );
};

export const useWishlist = () => {
  const context = React.useContext(WishlistContext);
  if (!context) {
    throw new Error('useWishlist must be used within a WishlistProvider');
  }
  return context;
};