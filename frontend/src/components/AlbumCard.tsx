import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Music, Calendar } from 'lucide-react';
import type { Album } from '../types/index';

interface AlbumCardProps {
    album: Album;
}

export const AlbumCard: React.FC<AlbumCardProps> = ({ album }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/album/${album.id}`);
    };

    const formatReleaseDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.getFullYear();
    };

    return (
        <div
            onClick={handleClick}
            className="rounded-lg hover:bg-gray-950 p-4 group"
        >
            <div className="flex items-center mb-3">
                {album.images[0].url ? (
                    <img
                        src={album.images[0].url}
                        alt={album.name}
                        className="w-16 h-16 rounded object-cover shadow-lg"
                    />
                ) : (
                    <div className="w-16 h-16 bg-gray-600 rounded flex items-center justify-center">
                        <Music className="w-8 h-8 text-gray-400" />
                    </div>
                )}

                <div className="ml-4 flex-1 min-w-0">
                    <h3 className="text-white font-semibold truncate group-hover:text-green-400 transition-colors">
                        {album.name}
                    </h3>
                    <p className="text-gray-400 text-sm truncate">
                        {album.artists && album.artists.length > 0
                            ? album.artists.map(artist => artist.name).join(', ')
                            : 'Unknown'}
                    </p>
                    <div className="flex items-center justify-center text-gray-500 text-xs mt-1">
                        <Calendar className="w-3 h-3 mr-1" />
                        <span>{formatReleaseDate(album.releaseDate)}</span>
                        <span className="mx-2">•</span>
                        <span>{album.totalTracks} tracks</span>
                    </div>
                </div>
            </div>
        </div>
    );
};