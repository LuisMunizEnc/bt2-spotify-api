import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Music, Calendar } from 'lucide-react';
import type { Album } from '../types/index';

interface AlbumCardProps {
    album: Album;
    variant?: 'horizontal' | 'vertical';
}

export const AlbumCard: React.FC<AlbumCardProps> = ({ album, variant = 'horizontal' }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/album/${album.id}`);
    };

    const formatReleaseDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.getFullYear();
    };

    const releaseYear = formatReleaseDate(album.release_date);

    return (
        <div
            onClick={handleClick}
            className={`rounded-lg hover:bg-gray-950 p-4 group cursor-pointer ${variant === 'vertical' ? 'w-40' : ''
                }`}
        >
            {variant === 'horizontal' ? (
                <div className="flex items-center mb-3">
                    {album.images.length !== 0 && album.images[0]?.url ? (
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
                            {album.artists?.map(artist => artist.name).join(', ') || 'Unknown'}
                        </p>
                        <div className="flex justify-center items-center text-gray-500 text-xs mt-1">
                            <Calendar className="w-3 h-3 mr-1" />
                            <span>{releaseYear}</span>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="flex flex-col items-start">
                    {album.images.length !== 0 && album.images[0]?.url ? (
                        <div className="aspect-square w-full overflow-hidden rounded shadow-lg">
                            <img
                                src={album.images[0].url}
                                alt={album.name}
                                className="w-full h-full object-cover"
                            />
                        </div>
                    ) : (
                        <div className="w-full h-40 bg-gray-600 rounded flex items-center justify-center mb-2">
                            <Music className="w-10 h-10 text-gray-400" />
                        </div>
                    )}
                    <h3 className="text-white font-medium text-sm truncate w-full group-hover:text-green-400 transition-colors">
                        {album.name}
                    </h3>
                    <p className="text-gray-400 text-xs w-full truncate">
                        {album.artists?.map(artist => artist.name).join(', ') || 'Unknown'}
                        <br />
                        {releaseYear}
                    </p>
                </div>
            )}
        </div>
    );
};