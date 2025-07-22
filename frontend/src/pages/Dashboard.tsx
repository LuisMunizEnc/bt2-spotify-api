import React, { useState, useEffect } from 'react';
import { TrendingUp } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Navigation } from '../components/Navigation';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { ArtistCard } from '../components/ArtistCard';
import { UserProfileCard } from '../components/UserProfileCard';
import { spotifyService } from '../service/SpotifyService';
import type { Artist, Track } from '../types';
import { TrackCard } from '../components/TrackCard';
import { Player } from '../components/Player';

export const Dashboard = () => {
    const { user } = useAuth();
    const [loading, setLoading] = useState(false);
    const [topArtists, setTopArtists] = useState<Artist[]>([]);
    const [topTracks, setTopTracks] = useState<Track[]>([]);

    const loadData = async () => {
        setLoading(true);
        try {
            const artistsData = await spotifyService.getTopArtists();
            setTopArtists(artistsData);
            const tracksData = await spotifyService.getTopTracks();
            setTopTracks(tracksData);
        } catch (error) {
            console.error('Failed to load data:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    return (
        <div className="min-h-screen bg-gradient-to-br from-black via-black to-green-900">
            <Navigation />

            <main className="max-w-7xl mx-auto p-6">
                {/* User Profile Card */}
                {user && (
                    <div className="mb-8">
                        <UserProfileCard user={user} />
                    </div>
                )}

                {/* Top Artists Section */}
                <div className="mb-8">
                    <div className="flex items-center mb-6">
                        <TrendingUp className="w-6 h-6 text-green-500 mr-3" />
                        <h2 className="text-3xl font-bold text-white">Your Top Artists</h2>
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center h-64">
                            <LoadingSpinner size="lg" />
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                            {topArtists.map((artist) => (
                                <ArtistCard key={artist.id} artist={artist} />
                            ))}
                        </div>
                    )}
                </div>

                {/* Top Tracks Section */}
                <div className="mb-8">
                    <div className="flex items-center mb-6">
                        <TrendingUp className="w-6 h-6 text-green-500 mr-3" />
                        <h2 className="text-3xl font-bold text-white">Your Top Tracks</h2>
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center h-64">
                            <LoadingSpinner size="lg" />
                        </div>
                    ) : topTracks.length > 0 ? (
                        <div className="space-y-2">
                            {topTracks.map((track, index) => (
                                <TrackCard key={track.id} track={track} index={index} showAlbum={true} />
                            ))}
                        </div>
                    ) : (
                        <p className="text-gray-400">No top tracks available.</p>
                    )}
                </div>
            </main>
            {/* <Player/> */}
        </div>
    );
};