import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { AuthGuard } from '@/components/auth/AuthGuard';
import { LoginForm } from '@/components/auth/LoginForm';
import { RegisterForm } from '@/components/auth/RegisterForm';
import { PasswordReset } from '@/components/auth/PasswordReset';
import { SessionTimeout } from '@/components/auth/SessionTimeout';
import { LoadingSpinner } from '@/components/ui';

type AuthMode = 'login' | 'register' | 'forgot-password';

export const AuthPage: React.FC = () => {
  const [mode, setMode] = useState<AuthMode>('login');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading } = useAuth();

  // Get mode from URL params
  useEffect(() => {
    const urlMode = searchParams.get('mode') as AuthMode;
    if (urlMode && ['login', 'register', 'forgot-password'].includes(urlMode)) {
      setMode(urlMode);
    }
  }, [searchParams]);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      const returnUrl = searchParams.get('returnUrl') || '/';
      navigate(returnUrl, { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate, searchParams]);

  // Show loading while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <LoadingSpinner size="lg" />
          <p className="mt-4 text-secondary-600 dark:text-secondary-400">
            Loading...
          </p>
        </div>
      </div>
    );
  }

  // Don't render if already authenticated
  if (isAuthenticated) {
    return null;
  }

  const renderForm = () => {
    switch (mode) {
      case 'login':
        return (
          <LoginForm
            onSwitchToRegister={() => setMode('register')}
            onSwitchToForgotPassword={() => setMode('forgot-password')}
          />
        );
      case 'register':
        return (
          <RegisterForm
            onSwitchToLogin={() => setMode('login')}
          />
        );
      case 'forgot-password':
        return (
          <PasswordReset
            onBack={() => setMode('login')}
          />
        );
      default:
        return (
          <LoginForm
            onSwitchToRegister={() => setMode('register')}
            onSwitchToForgotPassword={() => setMode('forgot-password')}
          />
        );
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-100 dark:from-secondary-900 dark:to-secondary-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo/Brand */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-primary-600 dark:text-primary-400">
            E-Commerce Store
          </h1>
          <p className="text-secondary-600 dark:text-secondary-400 mt-2">
            Your trusted shopping destination
          </p>
        </div>

        {/* Auth Form */}
        {renderForm()}

        {/* Footer */}
        <div className="text-center mt-8">
          <p className="text-sm text-secondary-500 dark:text-secondary-400">
            By continuing, you agree to our{' '}
            <a href="/terms" className="text-primary-600 hover:text-primary-500 dark:text-primary-400">
              Terms of Service
            </a>{' '}
            and{' '}
            <a href="/privacy" className="text-primary-600 hover:text-primary-500 dark:text-primary-400">
              Privacy Policy
            </a>
          </p>
        </div>
      </div>

      {/* Session Timeout Handler */}
      <SessionTimeout />
    </div>
  );
};

export default AuthPage;
