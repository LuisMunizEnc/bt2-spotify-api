import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User } from 'lucide-react';
import type { Artist } from '../types';

interface ArtistCardProps {
    artist: Artist;
}

export const ArtistCard: React.FC<ArtistCardProps> = ({ artist }) => {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/artist/${artist.id}`);
    };

    return (
        <div
            onClick={handleClick}
            className="ounded-lg p-4 hover:bg-gray-950 cursor-pointer group"
        >
            <div className="flex flex-col items-center text-center">
                {artist.images.length !== 0 && artist.images[0].url ? (
                    <img
                        src={artist.images[0].url}
                        alt={artist.name}
                        className="w-20 h-20 rounded-full object-cover shadow-lg mb-3"
                    />
                ) : (
                    <div className="w-20 h-20 bg-gray-600 rounded-full flex items-center justify-center mb-3">
                        <User className="w-10 h-10 text-gray-400" />
                    </div>
                )}

                <h3 className="text-white font-semibold truncate group-hover:text-green-400 transition-colors mb-1">
                    {artist.name}
                </h3>
            </div>
        </div>
    );
};