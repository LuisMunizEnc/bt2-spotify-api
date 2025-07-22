import React, { useState } from 'react';
import { Music } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { authService } from '../service/AuthService';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const { isAuthenticated, logout } = useAuth()

  const handleSpotifyLogin = async () => {
    setLoading(true);
    try {
      authService.initiateSpotifyLogin();
    } catch (error) {
      console.error('Login failed:', error);
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-green-900 flex items-center justify-center p-4">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">
            Breakable Toy II
          </h1>
        </div>

        <div className="bg-gray-900 rounded-lg p-8 shadow-2xl border border-gray-800">
          <Button
            onClick={handleSpotifyLogin}
            loading={loading}
            className="space-x-3"
            size="lg"
          >
            <Music className="w-5 h-5" />
            <span>Login with Spotify</span>
          </Button>
        </div>
        {isAuthenticated && <div>Hay token: <br />{authService.getToken()}</div>}
        <p className="text-gray-400 mt-6">
          Connect your Spotify account to view your music data
        </p>
      </div>
    </div>
  );
};