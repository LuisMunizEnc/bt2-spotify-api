export interface User {
    id: string;
    displayName: string;
    email: string;
    images?: SpotifyImage[];
    country?: string;
    product?: string;
}

export interface SpotifyImage{
    url: string;
    height: number; 
    width: number;
}

export interface Album{
    id: string;
    album_type: string;
    total_tracks: string;
    images: SpotifyImage[];
    name: string;
}

export interface Artist{
    id: string;
    name: string;
}

export interface Track{
    id: string;
    album: Album;
    artists: Artist[];
    durationMs: number;
    isPlayable: boolean;
    name: string;
}