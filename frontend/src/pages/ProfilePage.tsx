import React from 'react';
import { AuthGuard } from '@/components/auth/AuthGuard';
import { UserProfile } from '@/components/auth/UserProfile';
import { useAuth } from '@/contexts/AuthContext';
import { LoadingSpinner } from '@/components/ui';

export const ProfilePage: React.FC = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
            User not found
          </h2>
          <p className="text-secondary-600 dark:text-secondary-400">
            Please log in to view your profile.
          </p>
        </div>
      </div>
    );
  }

  return (
    <AuthGuard>
      <UserProfile user={user} />
    </AuthGuard>
  );
};

export default ProfilePage;