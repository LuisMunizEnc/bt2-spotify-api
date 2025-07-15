import React from 'react';
import { Play, Clock } from 'lucide-react';
import type { Track } from '../types';

interface TrackCardProps {
  track: Track;
  index: number;
}

export const TrackCard: React.FC<TrackCardProps> = ({ track, index }) => {
  const formatDuration = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className="flex items-center p-3 rounded-lg hover:bg-gray-950 transition-colors duration-200 w-full">
      <div className="flex-shrink-0 w-8 text-gray-400 text-sm font-medium">
        <span >{index + 1}</span>
      </div>
      
      <div className="flex-shrink-0 ml-4">
        {track.album.images[0].url ? (
          <img 
            src={track.album.images[0].url} 
            alt="track_image"
            className="w-10 h-10 rounded object-cover"
          />
        ) : (
          <div className="w-10 h-10 bg-gray-700 rounded flex items-center justify-center">
            <Play className="w-4 h-4 text-gray-400" />
          </div>
        )}
      </div>

      <div className="flex-1 min-w-0 ml-4 justify-items-start">
        <p className="text-white text-sm font-medium truncate">{track.name}</p>
        <p className="text-gray-400 text-xs truncate">
          {track.artists && track.artists.length > 0 ?
            track.artists.map(artist => artist.name).join(', ')
            : "Unknown"
          }
        </p>
      </div>

      <div className="flex-shrink-0 ml-4">
        <p className="text-gray-400 text-xs">{track.album.name}</p>
      </div>

      <div className="flex-shrink-0 ml-4 flex items-center">
        <Clock className="w-4 h-4 text-white mr-1" />
        <span className="text-white text-xs">{formatDuration(track.durationMs)}</span>
      </div>
    </div>
  );
};