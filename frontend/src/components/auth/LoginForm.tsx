import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, ArrowRight } from 'lucide-react';
import { Button, Input, Card, CardContent, CardHeader, CardTitle } from '@/components/ui';
import { GoogleLoginButton } from './GoogleLoginButton';
import { useLogin, useGoogleAuth } from '@/hooks/useAuth';
import { useNotifications } from '@/contexts/NotificationContext';
import TokenManager from '@/lib/tokenManager';

// Validation schema
const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().optional(),
});

type LoginForm = z.infer<typeof loginSchema>;

interface LoginFormProps {
  onSwitchToRegister?: () => void;
  onSwitchToForgotPassword?: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({
  onSwitchToRegister,
  onSwitchToForgotPassword,
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  const { showSuccess, showError } = useNotifications();
  const loginMutation = useLogin();
  const googleAuthMutation = useGoogleAuth();

  const form = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      rememberMe: false,
    },
  });

  // Get return URL from query params
  const returnUrl = searchParams.get('returnUrl') || '/';

  // Pre-fill email if remember me data exists
  useEffect(() => {
    const rememberMeData = TokenManager.getRememberMeData();
    if (rememberMeData) {
      form.setValue('email', rememberMeData.email);
      form.setValue('rememberMe', true);
    }
  }, [form]);

  // Handle form submission
  const onSubmit = async (data: LoginForm) => {
    setIsLoading(true);
    try {
      const response = await loginMutation.mutateAsync({
        email: data.email,
        password: data.password,
      });

      // Store remember me preference
      TokenManager.setRememberMe(data.email, data.rememberMe || false);

      // Store tokens
      TokenManager.setTokens({
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresAt: Date.now() + (response.data.expiresIn * 1000),
        user: response.data.user,
      });

      showSuccess(`Welcome back, ${response.data.user.firstName}!`);
      
      // Navigate to return URL or home
      navigate(returnUrl, { replace: true });
    } catch (error) {
      // Error is handled by the mutation
    } finally {
      setIsLoading(false);
    }
  };

  // Handle Google OAuth
  const handleGoogleAuth = async (googleToken: string) => {
    setIsLoading(true);
    try {
      const response = await googleAuthMutation.mutateAsync({
        token: googleToken,
      });

      // Store tokens
      TokenManager.setTokens({
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresAt: Date.now() + (response.data.expiresIn * 1000),
        user: response.data.user,
      });

      showSuccess(`Welcome, ${response.data.user.firstName}!`);
      
      // Navigate to return URL or home
      navigate(returnUrl, { replace: true });
    } catch (error) {
      // Error is handled by the mutation
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-bold">Welcome Back</CardTitle>
        <p className="text-secondary-600 dark:text-secondary-400">
          Sign in to your account to continue
        </p>
      </CardHeader>
      <CardContent>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {/* Email Field */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                id="email"
                type="email"
                placeholder="Enter your email"
                className="pl-10"
                {...form.register('email')}
              />
            </div>
            {form.formState.errors.email && (
              <p className="text-error-600 text-sm mt-1">
                {form.formState.errors.email.message}
              </p>
            )}
          </div>

          {/* Password Field */}
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                className="pl-10 pr-10"
                {...form.register('password')}
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-secondary-400 hover:text-secondary-600"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {form.formState.errors.password && (
              <p className="text-error-600 text-sm mt-1">
                {form.formState.errors.password.message}
              </p>
            )}
          </div>

          {/* Remember Me & Forgot Password */}
          <div className="flex items-center justify-between">
            <label className="flex items-center">
              <input
                type="checkbox"
                className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500"
                {...form.register('rememberMe')}
              />
              <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                Remember me
              </span>
            </label>
            
            {onSwitchToForgotPassword && (
              <button
                type="button"
                onClick={onSwitchToForgotPassword}
                className="text-sm text-primary-600 hover:text-primary-500 dark:text-primary-400 dark:hover:text-primary-300"
              >
                Forgot password?
              </button>
            )}
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            className="w-full"
            disabled={isLoading || loginMutation.isPending}
          >
            {isLoading || loginMutation.isPending ? (
              'Signing in...'
            ) : (
              <>
                Sign In
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>

          {/* Divider */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-secondary-300 dark:border-secondary-600" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white dark:bg-secondary-800 text-secondary-500">
                Or continue with
              </span>
            </div>
          </div>

          {/* Google OAuth */}
          <GoogleLoginButton
            onSuccess={handleGoogleAuth}
            disabled={isLoading || googleAuthMutation.isPending}
          />

          {/* Register Link */}
          {onSwitchToRegister && (
            <div className="text-center">
              <p className="text-sm text-secondary-600 dark:text-secondary-400">
                Don't have an account?{' '}
                <button
                  type="button"
                  onClick={onSwitchToRegister}
                  className="text-primary-600 hover:text-primary-500 dark:text-primary-400 dark:hover:text-primary-300 font-medium"
                >
                  Sign up
                </button>
              </p>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
};

export default LoginForm;