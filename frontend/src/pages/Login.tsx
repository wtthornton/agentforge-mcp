import { useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { LoginForm } from '../components/auth/LoginForm';

export function Login() {
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      window.location.href = '/';
    }
  }, [isAuthenticated]);

  if (isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            AgentForge
          </h1>
          <p className="text-gray-600">
            AI-Powered Development Platform
          </p>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <LoginForm 
          onSuccess={() => {
            window.location.href = '/';
          }}
        />
      </div>

      <div className="mt-8 text-center">
        <p className="text-xs text-gray-500">
          © 2025 AgentForge. All rights reserved.
        </p>
      </div>
    </div>
  );
}