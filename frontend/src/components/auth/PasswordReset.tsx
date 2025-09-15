import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Mail, Lock, CheckCircle, ArrowLeft } from 'lucide-react';
import { Button, Input, Card, CardContent, CardHeader, CardTitle } from '@/components/ui';
import { useForgotPassword, useResetPassword } from '@/hooks/useAuth';
import { useNotifications } from '@/contexts/NotificationContext';

// Validation schemas
const forgotPasswordSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
});

const resetPasswordSchema = z.object({
  token: z.string().min(1, 'Reset token is required'),
  newPassword: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type ForgotPasswordForm = z.infer<typeof forgotPasswordSchema>;
type ResetPasswordForm = z.infer<typeof resetPasswordSchema>;

interface PasswordResetProps {
  onBack?: () => void;
}

export const PasswordReset: React.FC<PasswordResetProps> = ({ onBack }) => {
  const [step, setStep] = useState<'forgot' | 'reset' | 'success'>('forgot');
  const [email, setEmail] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [resetToken, setResetToken] = useState('');

  const { showSuccess, showError } = useNotifications();
  const forgotPasswordMutation = useForgotPassword();
  const resetPasswordMutation = useResetPassword();

  // Forgot password form
  const forgotForm = useForm<ForgotPasswordForm>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  // Reset password form
  const resetForm = useForm<ResetPasswordForm>({
    resolver: zodResolver(resetPasswordSchema),
  });

  // Handle forgot password submission
  const onForgotPasswordSubmit = async (data: ForgotPasswordForm) => {
    try {
      await forgotPasswordMutation.mutateAsync(data);
      setEmail(data.email);
      setStep('reset');
      showSuccess('Password reset email sent! Please check your inbox.');
    } catch (error) {
      // Error is handled by the mutation
    }
  };

  // Handle reset password submission
  const onResetPasswordSubmit = async (data: ResetPasswordForm) => {
    try {
      await resetPasswordMutation.mutateAsync({
        token: resetToken,
        newPassword: data.newPassword,
      });
      setStep('success');
      showSuccess('Password reset successfully! You can now log in with your new password.');
    } catch (error) {
      // Error is handled by the mutation
    }
  };

  // Get reset token from URL parameters
  React.useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
      setResetToken(token);
      setStep('reset');
    }
  }, []);

  // Forgot Password Step
  if (step === 'forgot') {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">Forgot Password?</CardTitle>
          <p className="text-secondary-600 dark:text-secondary-400">
            Enter your email address and we'll send you a link to reset your password.
          </p>
        </CardHeader>
        <CardContent>
          <form onSubmit={forgotForm.handleSubmit(onForgotPasswordSubmit)} className="space-y-4">
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
                  {...forgotForm.register('email')}
                />
              </div>
              {forgotForm.formState.errors.email && (
                <p className="text-error-600 text-sm mt-1">
                  {forgotForm.formState.errors.email.message}
                </p>
              )}
            </div>

            <Button
              type="submit"
              className="w-full"
              disabled={forgotPasswordMutation.isPending}
            >
              {forgotPasswordMutation.isPending ? 'Sending...' : 'Send Reset Link'}
            </Button>

            {onBack && (
              <Button
                type="button"
                variant="outline"
                className="w-full"
                onClick={onBack}
              >
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Login
              </Button>
            )}
          </form>
        </CardContent>
      </Card>
    );
  }

  // Reset Password Step
  if (step === 'reset') {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">Reset Password</CardTitle>
          <p className="text-secondary-600 dark:text-secondary-400">
            Enter your new password below.
          </p>
        </CardHeader>
        <CardContent>
          <form onSubmit={resetForm.handleSubmit(onResetPasswordSubmit)} className="space-y-4">
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                New Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  id="newPassword"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter new password"
                  className="pl-10 pr-10"
                  {...resetForm.register('newPassword')}
                />
                <button
                  type="button"
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-secondary-400 hover:text-secondary-600"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {resetForm.formState.errors.newPassword && (
                <p className="text-error-600 text-sm mt-1">
                  {resetForm.formState.errors.newPassword.message}
                </p>
              )}
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                Confirm New Password
              </label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  id="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  placeholder="Confirm new password"
                  className="pl-10 pr-10"
                  {...resetForm.register('confirmPassword')}
                />
                <button
                  type="button"
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-secondary-400 hover:text-secondary-600"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {resetForm.formState.errors.confirmPassword && (
                <p className="text-error-600 text-sm mt-1">
                  {resetForm.formState.errors.confirmPassword.message}
                </p>
              )}
            </div>

            <Button
              type="submit"
              className="w-full"
              disabled={resetPasswordMutation.isPending}
            >
              {resetPasswordMutation.isPending ? 'Resetting...' : 'Reset Password'}
            </Button>

            <Button
              type="button"
              variant="outline"
              className="w-full"
              onClick={() => setStep('forgot')}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Email
            </Button>
          </form>
        </CardContent>
      </Card>
    );
  }

  // Success Step
  if (step === 'success') {
    return (
      <Card className="w-full max-w-md mx-auto">
        <CardHeader className="text-center">
          <div className="w-16 h-16 bg-success-100 dark:bg-success-900 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle className="h-8 w-8 text-success-600 dark:text-success-400" />
          </div>
          <CardTitle className="text-2xl font-bold">Password Reset Successful!</CardTitle>
          <p className="text-secondary-600 dark:text-secondary-400">
            Your password has been reset successfully. You can now log in with your new password.
          </p>
        </CardHeader>
        <CardContent>
          <Button
            className="w-full"
            onClick={() => window.location.href = '/login'}
          >
            Go to Login
          </Button>
        </CardContent>
      </Card>
    );
  }

  return null;
};

export default PasswordReset;
