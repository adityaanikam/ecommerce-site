import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { User, LoginRequest, RegisterRequest, AuthResponse, AuthContextType } from '@/types';
import { authService } from '@/services';

// Auth state interface
interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Auth actions
type AuthAction =
  | { type: 'AUTH_START' }
  | { type: 'AUTH_SUCCESS'; payload: { user: User; token: string; refreshToken: string } }
  | { type: 'AUTH_FAILURE'; payload: string }
  | { type: 'AUTH_LOGOUT' }
  | { type: 'AUTH_CLEAR_ERROR' }
  | { type: 'AUTH_UPDATE_USER'; payload: User };

// Initial state
const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('token'),
  refreshToken: localStorage.getItem('refreshToken'),
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Auth reducer
const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'AUTH_START':
      return {
        ...state,
        isLoading: true,
        error: null,
      };
    case 'AUTH_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };
    case 'AUTH_FAILURE':
      return {
        ...state,
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload,
      };
    case 'AUTH_LOGOUT':
      return {
        ...state,
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      };
    case 'AUTH_CLEAR_ERROR':
      return {
        ...state,
        error: null,
      };
    case 'AUTH_UPDATE_USER':
      return {
        ...state,
        user: action.payload,
      };
    default:
      return state;
  }
};

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider component
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Initialize auth state on mount
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          dispatch({ type: 'AUTH_START' });
          const user = await authService.getCurrentUser();
          dispatch({
            type: 'AUTH_SUCCESS',
            payload: {
              user,
              token,
              refreshToken: localStorage.getItem('refreshToken') || '',
            },
          });
        } catch (error) {
          dispatch({ type: 'AUTH_LOGOUT' });
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
        }
      }
    };

    initializeAuth();
  }, []);

  // Login function
  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      dispatch({ type: 'AUTH_START' });
      const response: AuthResponse = await authService.login(credentials);
      
      // Store tokens
      localStorage.setItem('token', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
      
      // Get user details
      const user = await authService.getCurrentUser();
      
      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          user,
          token: response.token,
          refreshToken: response.refreshToken,
        },
      });
    } catch (error: any) {
      dispatch({ type: 'AUTH_FAILURE', payload: error.message });
      throw error;
    }
  };

  // Register function
  const register = async (userData: RegisterRequest): Promise<void> => {
    try {
      dispatch({ type: 'AUTH_START' });
      const response: AuthResponse = await authService.register(userData);
      
      // Store tokens
      localStorage.setItem('token', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
      
      // Get user details
      const user = await authService.getCurrentUser();
      
      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          user,
          token: response.token,
          refreshToken: response.refreshToken,
        },
      });
    } catch (error: any) {
      dispatch({ type: 'AUTH_FAILURE', payload: error.message });
      throw error;
    }
  };

  // Logout function
  const logout = async (): Promise<void> => {
    try {
      await authService.logout();
    } catch (error) {
      // Continue with logout even if API call fails
      console.error('Logout API call failed:', error);
    } finally {
      // Clear local storage and state
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      dispatch({ type: 'AUTH_LOGOUT' });
    }
  };

  // Refresh token function
  const refreshToken = async (): Promise<void> => {
    try {
      const currentRefreshToken = state.refreshToken;
      if (!currentRefreshToken) {
        throw new Error('No refresh token available');
      }

      const response: AuthResponse = await authService.refreshToken(currentRefreshToken);
      
      // Update tokens
      localStorage.setItem('token', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
      
      // Update state
      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          user: state.user!,
          token: response.token,
          refreshToken: response.refreshToken,
        },
      });
    } catch (error) {
      // Refresh failed, logout user
      await logout();
      throw error;
    }
  };

  // Update profile function
  const updateProfile = async (data: Partial<User>): Promise<void> => {
    try {
      // This would typically call an API to update the user profile
      // For now, we'll just update the local state
      if (state.user) {
        const updatedUser = { ...state.user, ...data };
        dispatch({ type: 'AUTH_UPDATE_USER', payload: updatedUser });
      }
    } catch (error: any) {
      throw error;
    }
  };

  // Clear error function
  const clearError = (): void => {
    dispatch({ type: 'AUTH_CLEAR_ERROR' });
  };

  // Context value
  const value: AuthContextType = {
    user: state.user,
    token: state.token,
    isAuthenticated: state.isAuthenticated,
    isLoading: state.isLoading,
    login,
    register,
    logout,
    refreshToken,
    updateProfile,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
