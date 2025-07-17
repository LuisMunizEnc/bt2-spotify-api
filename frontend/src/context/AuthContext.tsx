import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { AuthContextType, User } from '../types';
import  { authService } from '../service/AuthService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = authService.getToken();
      
      if (storedToken) {
        try {
          setToken(storedToken);
          const userProfile = await authService.getUserProfile();
          setUser(userProfile);
        } catch (error) {
          console.error('Failed to obtain user profile:', error);
          authService.logout();
        }
      }
      
      setLoading(false);
    };

    initializeAuth();
  }, []);


  const login = async (newToken: string) => {
    setLoading(true);
    try {
      authService.setToken(newToken);
      setToken(newToken);

      const userProfile = await authService.getUserProfile();
      setUser(userProfile);
    } catch (error) {
      console.error('Login failed:', error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  const value: AuthContextType = {
    user,
    token,
    login,
    logout,
    isAuthenticated: authService.isAuthenticated(),
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};