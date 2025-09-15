import React from 'react';
import { useAuth } from '@/contexts/AuthContext';
import TokenManager from '@/lib/tokenManager';

interface RoleGuardProps {
  children: React.ReactNode;
  roles: string[];
  mode?: 'any' | 'all';
  fallback?: React.ReactNode;
  requireAuth?: boolean;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  roles,
  mode = 'any',
  fallback = null,
  requireAuth = true,
}) => {
  const { isAuthenticated } = useAuth();

  // If authentication is required but user is not authenticated
  if (requireAuth && !isAuthenticated) {
    return <>{fallback}</>;
  }

  // Check role requirements
  let hasPermission = false;

  if (mode === 'any') {
    hasPermission = TokenManager.hasAnyRole(roles);
  } else if (mode === 'all') {
    const userData = TokenManager.getUserData();
    if (userData) {
      hasPermission = roles.every(role => userData.roles.includes(role));
    }
  }

  return hasPermission ? <>{children}</> : <>{fallback}</>;
};

// Higher-order component for role-based rendering
export const withRoleGuard = <P extends object>(
  Component: React.ComponentType<P>,
  roles: string[],
  options: Omit<RoleGuardProps, 'children' | 'roles'> = {}
) => {
  const WrappedComponent = (props: P) => (
    <RoleGuard roles={roles} {...options}>
      <Component {...props} />
    </RoleGuard>
  );

  WrappedComponent.displayName = `withRoleGuard(${Component.displayName || Component.name})`;

  return WrappedComponent;
};

// Hook for role-based logic
export const useRoleGuard = () => {
  const { user, isAuthenticated } = useAuth();

  const hasRole = (role: string): boolean => {
    if (!isAuthenticated) return false;
    return TokenManager.hasRole(role);
  };

  const hasAnyRole = (roles: string[]): boolean => {
    if (!isAuthenticated) return false;
    return TokenManager.hasAnyRole(roles);
  };

  const hasAllRoles = (roles: string[]): boolean => {
    if (!isAuthenticated || !user) return false;
    return roles.every(role => user.roles.includes(role));
  };

  const isAdmin = (): boolean => {
    return hasRole('ADMIN');
  };

  const isModerator = (): boolean => {
    return hasRole('MODERATOR');
  };

  const isUser = (): boolean => {
    return hasRole('USER');
  };

  const canManageProducts = (): boolean => {
    return hasAnyRole(['ADMIN', 'MODERATOR']);
  };

  const canManageUsers = (): boolean => {
    return hasRole('ADMIN');
  };

  const canViewAnalytics = (): boolean => {
    return hasAnyRole(['ADMIN', 'MODERATOR']);
  };

  const canManageOrders = (): boolean => {
    return hasAnyRole(['ADMIN', 'MODERATOR']);
  };

  return {
    hasRole,
    hasAnyRole,
    hasAllRoles,
    isAdmin,
    isModerator,
    isUser,
    canManageProducts,
    canManageUsers,
    canViewAnalytics,
    canManageOrders,
  };
};

// Conditional rendering components
export const AdminOnly: React.FC<{ children: React.ReactNode; fallback?: React.ReactNode }> = ({
  children,
  fallback = null,
}) => (
  <RoleGuard roles={['ADMIN']} fallback={fallback}>
    {children}
  </RoleGuard>
);

export const ModeratorOnly: React.FC<{ children: React.ReactNode; fallback?: React.ReactNode }> = ({
  children,
  fallback = null,
}) => (
  <RoleGuard roles={['MODERATOR']} fallback={fallback}>
    {children}
  </RoleGuard>
);

export const AdminOrModerator: React.FC<{ children: React.ReactNode; fallback?: React.ReactNode }> = ({
  children,
  fallback = null,
}) => (
  <RoleGuard roles={['ADMIN', 'MODERATOR']} fallback={fallback}>
    {children}
  </RoleGuard>
);

export const AuthenticatedOnly: React.FC<{ children: React.ReactNode; fallback?: React.ReactNode }> = ({
  children,
  fallback = null,
}) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <>{fallback}</>;
};

export const GuestOnly: React.FC<{ children: React.ReactNode; fallback?: React.ReactNode }> = ({
  children,
  fallback = null,
}) => {
  const { isAuthenticated } = useAuth();
  return !isAuthenticated ? <>{children}</> : <>{fallback}</>;
};

export default RoleGuard;
