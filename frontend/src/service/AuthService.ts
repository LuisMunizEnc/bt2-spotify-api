import type { AxiosResponse } from 'axios';
import { API_CONFIG } from '../config/api';
import type { User } from '../types';
import axiosInstance from './AxiosInstance';

class AuthService {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('spotify_token');
  }

  initiateSpotifyLogin(): void {
    const loginUrl = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.AUTH.SPOTIFY_LOGIN}`;
    window.location.href = loginUrl;
  }

  setToken(token: string): void {
    this.token = token;
    localStorage.setItem('spotify_token', token);
  }

  getToken(): string | null {
    return this.token || localStorage.getItem('spotify_token');
  }

  async getUserProfile(): Promise<User> {
    const response: AxiosResponse<User> = await axiosInstance.get(
      `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USER.PROFILE}`
    );
    return response.data;
  }

  logout(): void {
    this.token = null;
    localStorage.removeItem('spotify_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}

export const authService = new AuthService();