import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { LoadingSpinner } from '@/components/ui';
import TokenManager from '@/lib/tokenManager';

interface AuthGuardProps {
  children: React.ReactNode;
  requiredRoles?: string[];
  fallbackPath?: string;
  requireAuth?: boolean;
}

export const AuthGuard: React.FC<AuthGuardProps> = ({
  children,
  requiredRoles = [],
  fallbackPath = '/login',
  requireAuth = true,
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const [isChecking, setIsChecking] = useState(true);
  const location = useLocation();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        // Initialize token manager
        const isValid = await TokenManager.initialize();
        
        if (!isValid && requireAuth) {
          // Redirect to login with return URL
          const returnUrl = encodeURIComponent(location.pathname + location.search);
          window.location.href = `${fallbackPath}?returnUrl=${returnUrl}`;
          return;
        }
        
        setIsChecking(false);
      } catch (error) {
        console.error('Auth check failed:', error);
        if (requireAuth) {
          window.location.href = fallbackPath;
        }
        setIsChecking(false);
      }
    };

    checkAuth();
  }, [requireAuth, fallbackPath, location.pathname, location.search]);

  // Show loading spinner while checking authentication
  if (isLoading || isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <LoadingSpinner size="lg" />
          <p className="mt-4 text-secondary-600 dark:text-secondary-400">
            Verifying authentication...
          </p>
        </div>
      </div>
    );
  }

  // If authentication is not required, render children
  if (!requireAuth) {
    return <>{children}</>;
  }

  // If not authenticated, redirect to login
  if (!isAuthenticated || !user) {
    const returnUrl = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`${fallbackPath}?returnUrl=${returnUrl}`} replace />;
  }

  // Check role requirements
  if (requiredRoles.length > 0) {
    const hasRequiredRole = TokenManager.hasAnyRole(requiredRoles);
    
    if (!hasRequiredRole) {
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center max-w-md mx-auto p-6">
            <div className="w-16 h-16 bg-error-100 dark:bg-error-900 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-error-600 dark:text-error-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-2">
              Access Denied
            </h2>
            <p className="text-secondary-600 dark:text-secondary-400 mb-4">
              You don't have permission to access this page. Required roles: {requiredRoles.join(', ')}
            </p>
            <button
              onClick={() => window.history.back()}
              className="btn btn-primary"
            >
              Go Back
            </button>
          </div>
        </div>
      );
    }
  }

  return <>{children}</>;
};

// Higher-order component for protecting routes
export const withAuthGuard = <P extends object>(
  Component: React.ComponentType<P>,
  options: Omit<AuthGuardProps, 'children'> = {}
) => {
  const WrappedComponent = (props: P) => (
    <AuthGuard {...options}>
      <Component {...props} />
    </AuthGuard>
  );

  WrappedComponent.displayName = `withAuthGuard(${Component.displayName || Component.name})`;

  return WrappedComponent;
};

// Hook for checking authentication status
export const useAuthGuard = () => {
  const { user, isAuthenticated } = useAuth();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      await TokenManager.initialize();
      setIsChecking(false);
    };

    checkAuth();
  }, []);

  const hasRole = (role: string) => {
    return TokenManager.hasRole(role);
  };

  const hasAnyRole = (roles: string[]) => {
    return TokenManager.hasAnyRole(roles);
  };

  const hasAllRoles = (roles: string[]) => {
    const userRoles = user?.roles || [];
    return roles.every(role => userRoles.includes(role));
  };

  return {
    isAuthenticated,
    user,
    isChecking,
    hasRole,
    hasAnyRole,
    hasAllRoles,
  };
};

export default AuthGuard;
