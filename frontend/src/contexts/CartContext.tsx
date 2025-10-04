import React from 'react';
import { Product } from '@/types/api';
import { getImageUrl, getProductImageUrl } from '@/config';

interface CartItem {
  productId: string;
  quantity: number;
  name: string;
  price: number;
  image: string;
  stock: number;
}

interface CartState {
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}

type CartAction =
  | { type: 'ADD_TO_CART'; payload: { product: Product; quantity: number } }
  | { type: 'REMOVE_FROM_CART'; payload: string }
  | { type: 'UPDATE_QUANTITY'; payload: { productId: string; quantity: number } }
  | { type: 'CLEAR_CART' }
  | { type: 'LOAD_CART'; payload: CartState };

import { STORAGE_KEYS } from '@/config';
const CART_STORAGE_KEY = STORAGE_KEYS.CART;

const initialState: CartState = {
  items: [],
  totalItems: 0,
  totalAmount: 0,
};

const CartContext = React.createContext<{
  state: CartState;
  addToCart: (product: Product, quantity?: number) => void;
  removeFromCart: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
} | null>(null);

const cartReducer = (state: CartState, action: CartAction): CartState => {
  switch (action.type) {
    case 'ADD_TO_CART': {
      const { product, quantity } = action.payload;
      const existingItem = state.items.find(item => item.productId === product.id);

        if (existingItem) {
        const newQuantity = Math.min(existingItem.quantity + quantity, product.stock);
        const items = state.items.map(item =>
          item.productId === product.id
            ? { ...item, quantity: newQuantity }
            : item
        );
        return {
          items,
          totalItems: items.reduce((sum, item) => sum + item.quantity, 0),
          totalAmount: items.reduce((sum, item) => sum + item.price * item.quantity, 0),
        };
      }

      const newItem: CartItem = {
        productId: product.id,
        quantity: Math.min(quantity, product.stock),
        name: product.name,
        price: product.price,
        image: getProductImageUrl(product, 0),
        stock: product.stock,
      };

      const items = [...state.items, newItem];
      return {
        items,
        totalItems: items.reduce((sum, item) => sum + item.quantity, 0),
        totalAmount: items.reduce((sum, item) => sum + item.price * item.quantity, 0),
      };
    }

    case 'REMOVE_FROM_CART': {
      const items = state.items.filter(item => item.productId !== action.payload);
      return {
        items,
        totalItems: items.reduce((sum, item) => sum + item.quantity, 0),
        totalAmount: items.reduce((sum, item) => sum + item.price * item.quantity, 0),
      };
    }

    case 'UPDATE_QUANTITY': {
      const items = state.items.map(item =>
        item.productId === action.payload.productId
          ? { ...item, quantity: Math.min(action.payload.quantity, item.stock) }
          : item
      );
      return {
        items,
        totalItems: items.reduce((sum, item) => sum + item.quantity, 0),
        totalAmount: items.reduce((sum, item) => sum + item.price * item.quantity, 0),
      };
    }

    case 'CLEAR_CART':
      return initialState;

    case 'LOAD_CART':
      return action.payload;

    default:
      return state;
  }
};

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = React.useReducer(cartReducer, initialState);

  // Load cart from localStorage on mount
  React.useEffect(() => {
    const savedCart = localStorage.getItem(CART_STORAGE_KEY);
    if (savedCart) {
      try {
        const parsedCart = JSON.parse(savedCart);
        dispatch({ type: 'LOAD_CART', payload: parsedCart });
      } catch (error) {
        console.error('Failed to parse cart from localStorage:', error instanceof Error ? error.message : String(error));
      }
    }
  }, []);

  // Save cart to localStorage whenever it changes
  React.useEffect(() => {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  const addToCart = (product: Product, quantity = 1) => {
    dispatch({ type: 'ADD_TO_CART', payload: { product, quantity } });
  };

  const removeFromCart = (productId: string) => {
    dispatch({ type: 'REMOVE_FROM_CART', payload: productId });
  };

  const updateQuantity = (productId: string, quantity: number) => {
    dispatch({ type: 'UPDATE_QUANTITY', payload: { productId, quantity } });
  };

  const clearCart = () => {
    dispatch({ type: 'CLEAR_CART' });
  };

  return (
    <CartContext.Provider
      value={{
        state,
    addToCart,
    removeFromCart,
        updateQuantity,
    clearCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = React.useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};