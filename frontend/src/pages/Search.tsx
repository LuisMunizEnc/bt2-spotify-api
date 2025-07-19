import { useEffect, useState } from 'react';
import { Search as SearchIcon } from 'lucide-react';
import { TrackCard } from '../components/TrackCard';
import { Button } from '../components/ui/Button'
import type { SearchResults } from '../types';
import { AlbumCard } from '../components/AlbumCard';
import { ArtistCard } from '../components/ArtistCard';
import { PlaylistCard } from '../components/PlaylistCard';
import { Navigation } from '../components/Navigation';
import { Card } from '../components/ui/Card';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { spotifyService } from '../service/SpotifyService';

function SearchPage() {

  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResults | null>(null);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setHasSearched(true);
    try {
      const searchResults = await spotifyService.search(query);
      setResults(searchResults);
    } catch (error) {
      console.error('Search failed:', error);
      setResults(null);
    } finally {
      setLoading(false);
    }
  };

  const renderSection = (title: string, items: any[], renderItem: (item: any, index: number) => React.ReactNode) => {
    if (!items || items.length === 0) return null;

    return (
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-white mb-4">{title}</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {items.slice(0, 8).map((item, index) => renderItem(item, index))}
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-green-900">
      <Navigation />

      {/* Search Bar */}
      <div className="max-w-7xl mx-auto p-6">
        <Card className="mb-8">
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="flex-1 relative">
              <SearchIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search for tracks, albums, artists, or playlists..."
                className="w-full pl-10 pr-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <Button type="submit" loading={loading} disabled={!query.trim()}>
              Search
            </Button>
          </form>
        </Card>

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center h-64">
            <LoadingSpinner size="lg" />
          </div>
        )}

        {/* Results */}
        {!loading && results && (
          <div>
            {/* Tracks */}
            {results.tracks && results.tracks.length > 0 && (
              <div className="mb-8">
                <h2 className="text-2xl font-bold text-white mb-4">Tracks</h2>
                <Card>
                  <div className="space-y-2">
                    {results.tracks.slice(0, 10).map((track, index) => (
                      <TrackCard key={track.id} track={track} index={index} />
                    ))}
                  </div>
                </Card>
              </div>
            )}

            {/* Artists */}
            {renderSection('Artists', results.artists, (artist) => (
              <ArtistCard key={artist.id} artist={artist} />
            ))}

            {/* Albums */}
            {renderSection('Albums', results.albums, (album) => (
              <AlbumCard key={album.id} album={album} />
            ))}

            {/* Playlists */}
            {renderSection('Playlists', results.playlists, (playlist) => (
              <PlaylistCard key={playlist.id} playlist={playlist} />
            ))}
          </div>
        )}

        {/* No Results */}
        {!loading && hasSearched && results &&
          !results.tracks?.length && !results.albums?.length &&
          !results.artists?.length && !results.playlists?.length && (
            <div className="text-center py-12">
              <SearchIcon className="w-16 h-16 text-gray-600 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-400 mb-2">No results found</h3>
              <p className="text-gray-500">Try searching with different keywords</p>
            </div>
          )}

        {/* Initial State */}
        {!hasSearched && (
          <div className="text-center py-12">
            <SearchIcon className="w-16 h-16 text-gray-600 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-400 mb-2">Search Spotify</h3>
            <p className="text-gray-500">Find your favorite tracks, albums, artists, and playlists</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchPage
