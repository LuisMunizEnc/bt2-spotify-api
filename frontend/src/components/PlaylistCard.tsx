import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Music } from 'lucide-react';
import type { Playlist } from '../types';

interface PlaylistCardProps {
  playlist: Playlist;
}

export const PlaylistCard: React.FC<PlaylistCardProps> = ({ playlist }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/playlist/${playlist.id}`);
  };

  return (
    <div 
      onClick={handleClick}
      className="rounded-lg p-4 hover:bg-gray-950 cursor-pointer group"
    >
      <div className="flex items-center mb-3">
        {playlist.images[0].url ? (
          <img 
            src={playlist.images[0].url} 
            alt={playlist.name}
            className="w-16 h-16 rounded object-cover shadow-lg"
          />
        ) : (
          <div className="w-16 h-16 bg-gray-600 rounded flex items-center justify-center">
            <Music className="w-8 h-8 text-gray-400" />
          </div>
        )}
        
        <div className="ml-4 flex-1 min-w-0">
          <h3 className="text-white font-semibold truncate group-hover:text-green-400 transition-colors">
            {playlist.name}
          </h3>
          <p className="text-gray-400 text-sm truncate">by {playlist.owner.display_name}</p>
          <div className="flex justify-center text-gray-500 text-xs mt-1">
            <span>{playlist.tracks.total} songs</span>
          </div>
        </div>
      </div>
      
      {playlist.description && (
        <p className="text-gray-400 text-sm line-clamp-2 mt-2">{playlist.description}</p>
      )}
    </div>
  );
};