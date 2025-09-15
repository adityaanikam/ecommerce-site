import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, User, Phone, ArrowRight } from 'lucide-react';
import { Button, Input, Card, CardContent, CardHeader, CardTitle } from '@/components/ui';
import { GoogleLoginButton } from './GoogleLoginButton';
import { useRegister, useGoogleAuth } from '@/hooks/useAuth';
import { useNotifications } from '@/contexts/NotificationContext';
import TokenManager from '@/lib/tokenManager';

// Validation schema
const registerSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  phoneNumber: z.string().optional(),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  confirmPassword: z.string(),
  agreeToTerms: z.boolean().refine(val => val === true, 'You must agree to the terms and conditions'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type RegisterForm = z.infer<typeof registerSchema>;

interface RegisterFormProps {
  onSwitchToLogin?: () => void;
}

export const RegisterForm: React.FC<RegisterFormProps> = ({ onSwitchToLogin }) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  
  const { showSuccess, showError } = useNotifications();
  const registerMutation = useRegister();
  const googleAuthMutation = useGoogleAuth();

  const form = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      agreeToTerms: false,
    },
  });

  // Handle form submission
  const onSubmit = async (data: RegisterForm) => {
    setIsLoading(true);
    try {
      const response = await registerMutation.mutateAsync({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
        phoneNumber: data.phoneNumber,
      });

      // Store tokens
      TokenManager.setTokens({
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresAt: Date.now() + (response.data.expiresIn * 1000),
        user: response.data.user,
      });

      showSuccess(`Welcome to our store, ${response.data.user.firstName}!`);
      
      // Navigate to home
      navigate('/', { replace: true });
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
      
      // Navigate to home
      navigate('/', { replace: true });
    } catch (error) {
      // Error is handled by the mutation
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-bold">Create Account</CardTitle>
        <p className="text-secondary-600 dark:text-secondary-400">
          Join us today and start shopping
        </p>
      </CardHeader>
      <CardContent>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {/* Name Fields */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                First Name
              </label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  id="firstName"
                  type="text"
                  placeholder="First name"
                  className="pl-10"
                  {...form.register('firstName')}
                />
              </div>
              {form.formState.errors.firstName && (
                <p className="text-error-600 text-sm mt-1">
                  {form.formState.errors.firstName.message}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                Last Name
              </label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  id="lastName"
                  type="text"
                  placeholder="Last name"
                  className="pl-10"
                  {...form.register('lastName')}
                />
              </div>
              {form.formState.errors.lastName && (
                <p className="text-error-600 text-sm mt-1">
                  {form.formState.errors.lastName.message}
                </p>
              )}
            </div>
          </div>

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

          {/* Phone Number Field */}
          <div>
            <label htmlFor="phoneNumber" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
              Phone Number (Optional)
            </label>
            <div className="relative">
              <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                id="phoneNumber"
                type="tel"
                placeholder="Enter your phone number"
                className="pl-10"
                {...form.register('phoneNumber')}
              />
            </div>
            {form.formState.errors.phoneNumber && (
              <p className="text-error-600 text-sm mt-1">
                {form.formState.errors.phoneNumber.message}
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
                placeholder="Create a password"
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

          {/* Confirm Password Field */}
          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
              Confirm Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="Confirm your password"
                className="pl-10 pr-10"
                {...form.register('confirmPassword')}
              />
              <button
                type="button"
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-secondary-400 hover:text-secondary-600"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
            {form.formState.errors.confirmPassword && (
              <p className="text-error-600 text-sm mt-1">
                {form.formState.errors.confirmPassword.message}
              </p>
            )}
          </div>

          {/* Terms and Conditions */}
          <div>
            <label className="flex items-start">
              <input
                type="checkbox"
                className="mt-1 rounded border-secondary-300 text-primary-600 focus:ring-primary-500"
                {...form.register('agreeToTerms')}
              />
              <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                I agree to the{' '}
                <a href="/terms" className="text-primary-600 hover:text-primary-500 dark:text-primary-400 dark:hover:text-primary-300">
                  Terms and Conditions
                </a>{' '}
                and{' '}
                <a href="/privacy" className="text-primary-600 hover:text-primary-500 dark:text-primary-400 dark:hover:text-primary-300">
                  Privacy Policy
                </a>
              </span>
            </label>
            {form.formState.errors.agreeToTerms && (
              <p className="text-error-600 text-sm mt-1">
                {form.formState.errors.agreeToTerms.message}
              </p>
            )}
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            className="w-full"
            disabled={isLoading || registerMutation.isPending}
          >
            {isLoading || registerMutation.isPending ? (
              'Creating Account...'
            ) : (
              <>
                Create Account
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

          {/* Login Link */}
          {onSwitchToLogin && (
            <div className="text-center">
              <p className="text-sm text-secondary-600 dark:text-secondary-400">
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={onSwitchToLogin}
                  className="text-primary-600 hover:text-primary-500 dark:text-primary-400 dark:hover:text-primary-300 font-medium"
                >
                  Sign in
                </button>
              </p>
            </div>
          )}
        </form>
      </CardContent>
    </Card>
  );
};

export default RegisterForm;