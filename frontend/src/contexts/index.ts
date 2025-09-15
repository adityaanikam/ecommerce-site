// Export all contexts
export { AuthProvider, useAuth } from './AuthContext';
export { CartProvider, useCart } from './CartContext';
export { ThemeProvider, useTheme } from './ThemeContext';

// Re-export types
export type { AuthContextType, CartContextType, ThemeContextType } from '@/types';
