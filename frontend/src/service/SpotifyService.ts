import { API_CONFIG } from '../config/api';
import axiosInstance from './AxiosInstance';
import type { AxiosResponse } from 'axios';
import type { Track, Playlist, Artist, Album, SearchResults, ArtistPageResults } from '../types';

class SpotifyService {
    async search(query: string): Promise<SearchResults> {
        const response: AxiosResponse<SearchResults> = await axiosInstance.get(
            `${API_CONFIG.BASE_URL}/search`,
            { params: { q: query } }
        );
        return response.data;
    }

    async artistPage(id: string): Promise<ArtistPageResults> {
        const response: AxiosResponse<ArtistPageResults> = await axiosInstance.get(
            `${API_CONFIG.BASE_URL}/artists/${id}`
        );
        return response.data;
    }

    async getTopArtists(): Promise<Artist[]> {
        const response: AxiosResponse<Artist[]> = await axiosInstance.get(
          `${API_CONFIG.BASE_URL}/artists/top`,
        );
        return response.data;
    }

    async getTopTracks(): Promise<Track[]> {
        const response: AxiosResponse<Track[]> = await axiosInstance.get(
          `${API_CONFIG.BASE_URL}/tracks/top`,
        );
        return response.data;
    }

    async getAlbum(id: string): Promise<Album> {
        const response: AxiosResponse<Album> = await axiosInstance.get(
            `${API_CONFIG.BASE_URL}/albums/${id}`
        );
        return response.data;
    }
}

export const spotifyService = new SpotifyService();