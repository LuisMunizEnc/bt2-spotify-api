export interface User {
    id: string;
    display_name: string;
    email: string;
    images?: SpotifyImage[];
    country?: string;
    product?: string;
    externalURL?: externalURLs;
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
    external_url: externalURLs;
    images: SpotifyImage[];
    name: string;
    artists: Artist[];
    release_date: string;
    track: Track[]
}

export interface Artist{
    id: string;
    name: string;
    images: SpotifyImage[];
    external_urls: externalURLs;
    followers: SpotifyFollowers;
}

export interface SpotifyFollowers{
    href: string;
    total: number;
}

export interface Track{
    id: string;
    album: Album;
    artists: Artist[];
    duration_ms: number;
    name: string;
    popularity?: number;
    preview_url?: string;
}

export interface Playlist{
    id: string;
    description: string;
    name: string;
    images: SpotifyImage[];
    owner: Owner;
    tracks: Track[] | SearchPlaylistTracks;
    external_urls: externalURLs;
}

export interface SearchPlaylistTracks{
    href: string;
    total: number;
}

export interface Owner{
    display_name: string;
}
export interface externalURLs{
    spotify: string;
}

export interface AuthContextType {
    user: User | null;
    token: string | null;
    login: (token: string) => void;
    logout: () => void;
    isAuthenticated: boolean;
    loading: boolean;
}

export interface SearchResults {
    tracks: Track[];
    albums: Album[];
    artists: Artist[];
    playlists: Playlist[];
}

export interface ArtistPageResults {
    artistProfile: Artist;
    topTracks: Track[];
    albums: Album[];
}