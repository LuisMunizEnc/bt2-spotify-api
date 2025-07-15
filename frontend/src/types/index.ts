export interface User {
    id: string;
    displayName: string;
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
    // TODO: limit tracks: handling
    id: string;
    albumType: string;
    totalTracks: string;
    externalURLs: externalURLs;
    images: SpotifyImage[];
    name: string;
    artists: Artist[];
    releaseDate: string;
    tracks: Track[]
}

export interface Artist{
    id: string;
    name: string;
    images: SpotifyImage[];
    externalURLs: externalURLs;
}

export interface Track{
    id: string;
    album: Album;
    artists: Artist[];
    durationMs: number;
    isPlayable?: boolean;
    name: string;
    popularity?: number;
    previewURL?: string;
}

export interface Playlist{
    id: string;
    description: string;
    name: string;
    images: SpotifyImage[];
    owner: Owner;
    tracks: Track[];
    externalURLs: externalURLs;
}

export interface Owner{
    displayName: string;
}
export interface externalURLs{
    spotify: string;
}