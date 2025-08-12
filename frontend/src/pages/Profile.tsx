import { UserProfile } from '../components/auth/UserProfile';

export function Profile() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Profile</h1>
        <p className="mt-1 text-sm text-gray-500">
          Manage your account settings and view your profile information.
        </p>
      </div>

      <UserProfile />
    </div>
  );
}