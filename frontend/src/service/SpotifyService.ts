import { API_CONFIG } from '../config/api';
import axiosInstance from './AxiosInstance';
import type { AxiosResponse } from 'axios';
import type { Track, Playlist, Artist, Album, SearchResults } from '../types';

class SpotifyService {
    async search(query: string): Promise<SearchResults> {
        const response: AxiosResponse<SearchResults> = await axiosInstance.get(
            `${API_CONFIG.BASE_URL}/search`,
            { params: { q: query } }
        );
        console.log(response.data);
        return response.data;
    }
}

export const spotifyService = new SpotifyService();