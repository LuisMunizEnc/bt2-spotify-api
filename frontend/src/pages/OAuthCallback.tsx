import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const OAuthCallback: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuth();

  useEffect(() => {
    const handleCallback = async () => {
      const token = searchParams.get('token');  
      
      if (token) {
        try {
          await login(token);
          navigate('/search', { replace: true });
        } catch (error) {
          console.error('Login failed on ', error);
          navigate('/login', { replace: true });
        }
      } else {
        navigate('/login', { replace: true });
      }
    };

    handleCallback();

  }, [searchParams, login, navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-green-900 flex items-center justify-center">
      <div className="text-center place-content-center">
        <p className="text-white mt-4 text-lg">Completing login...</p>
        <p className="text-gray-400 mt-2">Please wait while we authenticate you with Spotify</p>
      </div>
    </div>
  );
};