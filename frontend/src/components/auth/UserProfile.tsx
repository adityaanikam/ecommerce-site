import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Mail, Phone, Calendar, MapPin, Camera, Save, X } from 'lucide-react';
import { Button, Input, Card, CardContent, CardHeader, CardTitle, Avatar } from '@/components/ui';
import { useUpdateProfile, useChangePassword } from '@/hooks/useAuth';
import { useNotifications } from '@/contexts/NotificationContext';
import { UserDto } from '@/types/api';

// Validation schemas
const profileSchema = z.object({
  firstName: z.string().min(2, 'First name must be at least 2 characters'),
  lastName: z.string().min(2, 'Last name must be at least 2 characters'),
  email: z.string().email('Please enter a valid email address'),
  phoneNumber: z.string().optional(),
  dateOfBirth: z.string().optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']).optional(),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type ProfileForm = z.infer<typeof profileSchema>;
type PasswordForm = z.infer<typeof passwordSchema>;

interface UserProfileProps {
  user: UserDto;
  onUpdate?: (updatedUser: UserDto) => void;
}

export const UserProfile: React.FC<UserProfileProps> = ({ user, onUpdate }) => {
  const [activeTab, setActiveTab] = useState<'profile' | 'password' | 'preferences'>('profile');
  const [isEditing, setIsEditing] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  
  const { showSuccess, showError } = useNotifications();
  const updateProfileMutation = useUpdateProfile();
  const changePasswordMutation = useChangePassword();

  const profileForm = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      phoneNumber: user.phoneNumber,
      dateOfBirth: user.dateOfBirth,
      gender: user.gender,
    },
  });

  const passwordForm = useForm<PasswordForm>({
    resolver: zodResolver(passwordSchema),
  });

  // Handle profile update
  const onProfileSubmit = async (data: ProfileForm) => {
    try {
      const response = await updateProfileMutation.mutateAsync(data);
      onUpdate?.(response.data);
      setIsEditing(false);
      showSuccess('Profile updated successfully');
    } catch (error) {
      // Error is handled by the mutation
    }
  };

  // Handle password change
  const onPasswordSubmit = async (data: PasswordForm) => {
    try {
      await changePasswordMutation.mutateAsync({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      passwordForm.reset();
      showSuccess('Password changed successfully');
    } catch (error) {
      // Error is handled by the mutation
    }
  };

  // Handle avatar upload
  const handleAvatarUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setAvatarPreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const tabs = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'password', label: 'Password', icon: Mail },
    { id: 'preferences', label: 'Preferences', icon: Calendar },
  ] as const;

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100">
          Account Settings
        </h1>
        <p className="text-secondary-600 dark:text-secondary-400 mt-2">
          Manage your account information and preferences
        </p>
      </div>

      {/* Avatar Section */}
      <Card className="mb-6">
        <CardContent className="p-6">
          <div className="flex items-center gap-6">
            <div className="relative">
              <Avatar
                src={avatarPreview || user.avatar}
                alt={`${user.firstName} ${user.lastName}`}
                className="w-24 h-24"
              />
              <label className="absolute bottom-0 right-0 bg-primary-600 text-white rounded-full p-2 cursor-pointer hover:bg-primary-700">
                <Camera className="h-4 w-4" />
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarUpload}
                  className="hidden"
                />
              </label>
            </div>
            <div>
              <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100">
                {user.firstName} {user.lastName}
              </h2>
              <p className="text-secondary-600 dark:text-secondary-400">
                {user.email}
              </p>
              <p className="text-sm text-secondary-500 dark:text-secondary-400">
                Member since {new Date(user.createdAt).toLocaleDateString()}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <div className="border-b border-secondary-200 dark:border-secondary-700 mb-6">
        <nav className="flex space-x-8">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-primary-500 text-primary-600 dark:text-primary-400'
                    : 'border-transparent text-secondary-500 hover:text-secondary-700 hover:border-secondary-300 dark:text-secondary-400 dark:hover:text-secondary-300'
                }`}
              >
                <Icon className="h-4 w-4" />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Profile Tab */}
      {activeTab === 'profile' && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Profile Information</CardTitle>
              {!isEditing ? (
                <Button onClick={() => setIsEditing(true)}>
                  Edit Profile
                </Button>
              ) : (
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    onClick={() => {
                      setIsEditing(false);
                      profileForm.reset();
                    }}
                  >
                    <X className="h-4 w-4 mr-2" />
                    Cancel
                  </Button>
                  <Button
                    onClick={profileForm.handleSubmit(onProfileSubmit)}
                    disabled={updateProfileMutation.isPending}
                  >
                    <Save className="h-4 w-4 mr-2" />
                    {updateProfileMutation.isPending ? 'Saving...' : 'Save Changes'}
                  </Button>
                </div>
              )}
            </div>
          </CardHeader>
          <CardContent>
            <form className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    First Name
                  </label>
                  <Input
                    {...profileForm.register('firstName')}
                    disabled={!isEditing}
                    className={!isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : ''}
                  />
                  {profileForm.formState.errors.firstName && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.firstName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    Last Name
                  </label>
                  <Input
                    {...profileForm.register('lastName')}
                    disabled={!isEditing}
                    className={!isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : ''}
                  />
                  {profileForm.formState.errors.lastName && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.lastName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    Email Address
                  </label>
                  <Input
                    type="email"
                    {...profileForm.register('email')}
                    disabled={!isEditing}
                    className={!isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : ''}
                  />
                  {profileForm.formState.errors.email && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.email.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    Phone Number
                  </label>
                  <Input
                    type="tel"
                    {...profileForm.register('phoneNumber')}
                    disabled={!isEditing}
                    className={!isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : ''}
                  />
                  {profileForm.formState.errors.phoneNumber && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.phoneNumber.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    Date of Birth
                  </label>
                  <Input
                    type="date"
                    {...profileForm.register('dateOfBirth')}
                    disabled={!isEditing}
                    className={!isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : ''}
                  />
                  {profileForm.formState.errors.dateOfBirth && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.dateOfBirth.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                    Gender
                  </label>
                  <select
                    {...profileForm.register('gender')}
                    disabled={!isEditing}
                    className={`w-full px-3 py-2 border border-secondary-300 dark:border-secondary-600 rounded-md ${
                      !isEditing ? 'bg-secondary-50 dark:bg-secondary-800' : 'bg-white dark:bg-secondary-700'
                    }`}
                  >
                    <option value="">Select gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                  {profileForm.formState.errors.gender && (
                    <p className="text-error-600 text-sm mt-1">
                      {profileForm.formState.errors.gender.message}
                    </p>
                  )}
                </div>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Password Tab */}
      {activeTab === 'password' && (
        <Card>
          <CardHeader>
            <CardTitle>Change Password</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                  Current Password
                </label>
                <Input
                  type="password"
                  {...passwordForm.register('currentPassword')}
                />
                {passwordForm.formState.errors.currentPassword && (
                  <p className="text-error-600 text-sm mt-1">
                    {passwordForm.formState.errors.currentPassword.message}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                  New Password
                </label>
                <Input
                  type="password"
                  {...passwordForm.register('newPassword')}
                />
                {passwordForm.formState.errors.newPassword && (
                  <p className="text-error-600 text-sm mt-1">
                    {passwordForm.formState.errors.newPassword.message}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1">
                  Confirm New Password
                </label>
                <Input
                  type="password"
                  {...passwordForm.register('confirmPassword')}
                />
                {passwordForm.formState.errors.confirmPassword && (
                  <p className="text-error-600 text-sm mt-1">
                    {passwordForm.formState.errors.confirmPassword.message}
                  </p>
                )}
              </div>

              <Button
                type="submit"
                disabled={changePasswordMutation.isPending}
              >
                {changePasswordMutation.isPending ? 'Changing Password...' : 'Change Password'}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Preferences Tab */}
      {activeTab === 'preferences' && (
        <Card>
          <CardHeader>
            <CardTitle>Preferences</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-medium text-secondary-900 dark:text-secondary-100 mb-4">
                  Notification Preferences
                </h3>
                <div className="space-y-3">
                  <label className="flex items-center">
                    <input type="checkbox" className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" defaultChecked />
                    <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                      Email notifications for order updates
                    </span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" defaultChecked />
                    <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                      Promotional emails and offers
                    </span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" />
                    <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                      SMS notifications
                    </span>
                  </label>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-medium text-secondary-900 dark:text-secondary-100 mb-4">
                  Privacy Settings
                </h3>
                <div className="space-y-3">
                  <label className="flex items-center">
                    <input type="checkbox" className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" defaultChecked />
                    <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                      Allow profile to be visible to other users
                    </span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" className="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" />
                    <span className="ml-2 text-sm text-secondary-600 dark:text-secondary-400">
                      Allow marketing communications
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default UserProfile;
