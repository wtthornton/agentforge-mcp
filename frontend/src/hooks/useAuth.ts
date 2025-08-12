import { useState, useEffect, useContext, createContext, ReactNode } from 'react';
import { authService, LoginCredentials, UserProfile } from '../services/auth';

interface AuthContextType {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<{ success: boolean; message: string }>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = user !== null && authService.isAuthenticated();

  useEffect(() => {
    initializeAuth();
  }, []);

  const initializeAuth = async () => {
    setIsLoading(true);
    
    try {
      const storedUser = authService.getStoredUser();
      
      if (storedUser && authService.isAuthenticated()) {
        const currentUser = await authService.getCurrentUser();
        setUser(currentUser || storedUser);
      } else {
        authService.logout();
        setUser(null);
      }
    } catch (error) {
      console.error('Auth initialization failed:', error);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (credentials: LoginCredentials): Promise<{ success: boolean; message: string }> => {
    try {
      setIsLoading(true);
      const response = await authService.login(credentials);
      
      if (response.status === 'SUCCESS' && response.user) {
        setUser({
          id: response.user.id,
          username: response.user.username,
          email: response.user.email,
          role: response.user.role,
          isActive: true,
          createdAt: '',
          lastLogin: new Date().toISOString(),
        });
        
        return { success: true, message: response.message };
      } else {
        return { success: false, message: response.message };
      }
    } catch (error) {
      console.error('Login failed:', error);
      return { 
        success: false, 
        message: error instanceof Error ? error.message : 'Login failed' 
      };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    setIsLoading(true);
    
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      setIsLoading(false);
    }
  };

  const refreshUser = async (): Promise<void> => {
    if (!authService.isAuthenticated()) {
      return;
    }

    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
    } catch (error) {
      console.error('Failed to refresh user:', error);
      setUser(null);
    }
  };

  const contextValue: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    refreshUser,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export function useRequireAuth() {
  const { isAuthenticated, isLoading } = useAuth();
  
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      window.location.href = '/login';
    }
  }, [isAuthenticated, isLoading]);

  return { isAuthenticated, isLoading };
}

export function useAuthenticatedFetch() {
  const { logout } = useAuth();

  return async <T = any>(url: string, options: RequestInit = {}): Promise<T> => {
    try {
      return await authService.makeAuthenticatedRequest<T>(url, options);
    } catch (error) {
      if (error instanceof Error && error.message === 'Authentication required') {
        await logout();
        window.location.href = '/login';
        throw error;
      }
      throw error;
    }
  };
}