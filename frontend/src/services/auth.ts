interface LoginCredentials {
  usernameOrEmail: string;
  password: string;
}

interface AuthResponse {
  status: string;
  message: string;
  user?: {
    id: string;
    username: string;
    email: string;
    role: string;
  };
  accessToken?: string;
  refreshToken?: string;
  sessionId?: string;
}

interface TokenRefreshResponse {
  status: string;
  message: string;
  accessToken?: string;
  user?: {
    id: string;
    username: string;
    role: string;
  };
}

interface UserProfile {
  id: string;
  username: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: string;
  lastLogin: string;
}

class AuthService {
  private readonly BASE_URL = '/api/auth';
  
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await fetch(`${this.BASE_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    const data = await response.json();
    
    if (data.status === 'SUCCESS') {
      this.storeTokens(data.accessToken, data.refreshToken);
      this.storeUser(data.user);
    }
    
    return data;
  }

  async logout(): Promise<void> {
    const accessToken = this.getAccessToken();
    const refreshToken = this.getRefreshToken();

    if (accessToken && refreshToken) {
      try {
        await fetch(`${this.BASE_URL}/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            accessToken,
            refreshToken,
          }),
        });
      } catch (error) {
        console.warn('Error during logout:', error);
      }
    }

    this.clearTokens();
    this.clearUser();
  }

  async refreshToken(): Promise<TokenRefreshResponse | null> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return null;
    }

    try {
      const response = await fetch(`${this.BASE_URL}/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      const data = await response.json();
      
      if (data.status === 'SUCCESS') {
        this.storeTokens(data.accessToken, refreshToken);
        if (data.user) {
          this.updateStoredUser(data.user);
        }
      }
      
      return data;
    } catch (error) {
      console.error('Token refresh failed:', error);
      this.clearTokens();
      this.clearUser();
      return null;
    }
  }

  async getCurrentUser(): Promise<UserProfile | null> {
    const accessToken = this.getAccessToken();
    
    if (!accessToken) {
      return null;
    }

    try {
      const response = await fetch(`${this.BASE_URL}/me`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (response.ok) {
        return await response.json();
      } else if (response.status === 401) {
        const refreshed = await this.refreshToken();
        if (refreshed?.status === 'SUCCESS') {
          return this.getCurrentUser();
        }
      }
      
      return null;
    } catch (error) {
      console.error('Failed to get current user:', error);
      return null;
    }
  }

  async validateToken(token?: string): Promise<boolean> {
    const tokenToValidate = token || this.getAccessToken();
    
    if (!tokenToValidate) {
      return false;
    }

    try {
      const response = await fetch(`${this.BASE_URL}/validate?token=${encodeURIComponent(tokenToValidate)}`);
      const data = await response.json();
      return data.valid === true;
    } catch (error) {
      console.error('Token validation failed:', error);
      return false;
    }
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return token !== null && !this.isTokenExpired(token);
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  getStoredUser(): UserProfile | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  private storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  private storeUser(user: any): void {
    localStorage.setItem('user', JSON.stringify(user));
  }

  private updateStoredUser(userUpdate: Partial<UserProfile>): void {
    const currentUser = this.getStoredUser();
    if (currentUser) {
      const updatedUser = { ...currentUser, ...userUpdate };
      this.storeUser(updatedUser);
    }
  }

  private clearTokens(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  private clearUser(): void {
    localStorage.removeItem('user');
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000;
      return Date.now() >= exp;
    } catch {
      return true;
    }
  }

  getAuthorizationHeader(): string | null {
    const token = this.getAccessToken();
    return token ? `Bearer ${token}` : null;
  }

  async makeAuthenticatedRequest<T = any>(
    url: string, 
    options: RequestInit = {}
  ): Promise<T> {
    const authHeader = this.getAuthorizationHeader();
    
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
        ...(authHeader && { 'Authorization': authHeader }),
      },
    });

    if (response.status === 401) {
      const refreshed = await this.refreshToken();
      if (refreshed?.status === 'SUCCESS') {
        const newAuthHeader = this.getAuthorizationHeader();
        const retryResponse = await fetch(url, {
          ...options,
          headers: {
            'Content-Type': 'application/json',
            ...options.headers,
            ...(newAuthHeader && { 'Authorization': newAuthHeader }),
          },
        });
        
        if (!retryResponse.ok) {
          throw new Error(`Request failed: ${retryResponse.statusText}`);
        }
        
        return retryResponse.json();
      } else {
        this.clearTokens();
        this.clearUser();
        throw new Error('Authentication required');
      }
    }

    if (!response.ok) {
      throw new Error(`Request failed: ${response.statusText}`);
    }

    return response.json();
  }
}

export const authService = new AuthService();

export type {
  LoginCredentials,
  AuthResponse,
  TokenRefreshResponse,
  UserProfile,
};