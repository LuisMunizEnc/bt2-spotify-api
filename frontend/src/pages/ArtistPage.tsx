import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Users, Play, Music } from 'lucide-react';
import { Navigation } from '../components/Navigation';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { TrackCard } from '../components/TrackCard';
import { AlbumCard } from '../components/AlbumCard';
import { spotifyService } from '../service/SpotifyService';
import type { ArtistPageResults } from '../types';

export const ArtistPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [artistInfo, setArtistInfo] = useState<ArtistPageResults | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadArtistData = async () => {
      if (!id) return;
      
      setLoading(true);
      try {
        const artistResults = await spotifyService.artistPage(id);        
        setArtistInfo(artistResults);
      } catch (error) {
        console.error('Failed to load artist data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadArtistData();
  }, [id]);

  const formatFollowers = (count: number) => {
    if (count >= 1000000) {
      return `${(count / 1000000).toFixed(1)}M`;
    } else if (count >= 1000) {
      return `${(count / 1000).toFixed(1)}K`;
    }
    return count.toString();
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900 flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!artistInfo?.artistProfile) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-white mb-4">Artist not found</h2>
          <Button onClick={() => navigate(-1)}>Go Back</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-green-900">
      <Navigation />

      <main className="max-w-7xl mx-auto p-6 mt-16">
        {/* Artist Header */}
        <div className="mb-8">
          <div className="flex flex-col md:flex-row items-start md:items-end space-y-4 md:space-y-0 md:space-x-6">
            {artistInfo.artistProfile && artistInfo.artistProfile.images.length !== 0 ? (
              <img 
                src={artistInfo.artistProfile.images[0].url} 
                alt={artistInfo.artistProfile.name}
                className="w-48 h-48 rounded-full object-cover shadow-2xl"
              />
            ) : (
              <div className="w-48 h-48 bg-gray-600 rounded-full flex items-center justify-center">
                <Music className="w-24 h-24 text-gray-400" />
              </div>
            )}
            
            <div className="flex-1 text-left">
              <p className="text-gray-400 text-sm font-medium mb-2">ARTIST</p>
              <h1 className="text-4xl md:text-6xl font-bold text-white mb-4">{artistInfo.artistProfile.name}</h1>
              
              <div className="flex items-center space-x-6 text-gray-300">
                <div className="flex items-center">
                  <Users className="w-5 h-5 mr-2" />
                  <span>{formatFollowers(artistInfo.artistProfile.followers.total)} followers</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Albums */}
        {artistInfo.albums.length > 0 && (
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-white mb-4">Albums</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 place-items-center">
              {artistInfo.albums.map((album) => (
                <AlbumCard key={album.id} album={album} variant='vertical'/>
              ))}
            </div>
          </div>
        )}

        {/* Top Tracks */}
        {artistInfo.topTracks.length > 0 && (
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center">
              <Play className="w-6 h-6 mr-3 text-green-500" />
              Popular Tracks
            </h2>
            <Card>
              <div className="space-y-2">
                {artistInfo.topTracks.map((track, index) => (
                  <TrackCard key={track.id} track={track} index={index} showAlbum={true} />
                ))}
              </div>
            </Card>
          </div>
        )}
      </main>
    </div>
  );
};