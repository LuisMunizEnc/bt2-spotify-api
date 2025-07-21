import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Calendar, Clock, Music } from 'lucide-react';
import { Navigation } from '../components/Navigation';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { TrackCard } from '../components/TrackCard';
import { spotifyService } from '../service/SpotifyService';
import type { Album, Track } from '../types';

export const AlbumPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [album, setAlbum] = useState<Album | null>(null);
  const [tracks, setTracks] = useState<Track[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadAlbumData = async () => {
      if (!id) return;
      
      setLoading(true);
      try {
        const albumData = await spotifyService.getAlbum(id);
        setAlbum(albumData);
        setTracks(albumData?.track);
      } catch (error) {
        console.error('Failed to load album data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadAlbumData();
  }, [id]);

  const formatReleaseDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.getFullYear();
  };

  const getTotalDuration = () => {
    const totalMs = tracks.reduce((sum, track) => sum + track.duration_ms, 0);
    const totalMinutes = Math.floor(totalMs / 60000);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    
    if (hours > 0) {
      return `${hours} hr ${minutes} min`;
    }
    return `${minutes} min`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900 flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!album) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-white mb-4">Album not found</h2>
          <Button onClick={() => navigate(-1)}>Go Back</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900">
      <Navigation />

      <main className="max-w-7xl mx-auto p-6 mt-16">
        {/* Album Header */}
        <div className="mb-8">
          <div className="flex flex-col md:flex-row items-start md:items-end space-y-4 md:space-y-0 md:space-x-6">
            {album.images.length !== 0 && album.images[0].url ? (
              <img 
                src={album.images[0].url} 
                alt={album.name}
                className="w-48 h-48 rounded-lg object-cover shadow-2xl"
              />
            ) : (
              <div className="w-48 h-48 bg-gray-600 rounded-lg flex items-center justify-center">
                <Music className="w-24 h-24 text-gray-400" />
              </div>
            )}
            
            <div className="flex-1">
              <h1 className="text-left text-4xl md:text-6xl font-bold text-white mb-4">{album.name}</h1>
              
              <div className="flex items-center space-x-2 text-gray-300 mb-4">
                <span className="font-medium">
                    {album.artists?.map(artist => artist.name).join(', ') || 'Unknown'}
                </span>
                <span>•</span>
                <div className="flex items-center">
                  <Calendar className="w-4 h-4 mr-1" />
                  <span>{formatReleaseDate(album.release_date)}</span>
                </div>
                <span>•</span>
                <span>{album.total_tracks} songs</span>
                {tracks.length > 0 && (
                  <>
                    <span>•</span>
                    <div className="flex items-center">
                      <Clock className="w-4 h-4 mr-1" />
                      <span>{getTotalDuration()}</span>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Track List */}
        {tracks.length > 0 && (
          <Card>
            <div className="space-y-2">
              {tracks.map((track, index) => (
                <TrackCard key={track.id} track={track} index={index} showAlbum={false} />
              ))}
            </div>
          </Card>
        )}
      </main>
    </div>
  );
};