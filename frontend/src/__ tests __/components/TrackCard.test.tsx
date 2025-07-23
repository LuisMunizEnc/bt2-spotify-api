import React from 'react';
import { render, screen } from '@testing-library/react';
import { TrackCard } from '../../components/TrackCard';
import { Track, Album, Artist, SpotifyImage } from '../../types';

jest.mock('lucide-react', () => ({
  Play: ({ className }: { className: string }) => (
    <svg data-testid="play-icon" className={className} />
  ),
  Clock: ({ className }: { className: string }) => (
    <svg data-testid="clock-icon" className={className} />
  ),
}));

const formatDuration = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
};

const mockAlbumWithImage: Album = {
  id: 'album1',
  album_type: 'album',
  total_tracks: '10',
  external_url: { spotify: 'url' },
  images: [{ url: 'https://example.com/album-art.jpg', height: 64, width: 64 }],
  name: 'Test Album',
  artists: [],
  release_date: '2023-01-01',
  track: [],
};

const mockAlbumWithoutImage: Album = {
  id: 'album2',
  album_type: 'album',
  total_tracks: '5',
  external_url: { spotify: 'url' },
  images: [],
  name: 'Another Album',
  artists: [],
  release_date: '2022-01-01',
  track: [],
};

const mockArtist: Artist = {
  id: 'artist1',
  name: 'Test Artist',
  images: [],
  external_urls: { spotify: 'url' },
  followers: { href: '', total: 0 },
};

const mockTrack: Track = {
  id: 'track123',
  album: mockAlbumWithImage,
  artists: [mockArtist],
  duration_ms: 210000,
  name: 'Awesome Song',
  popularity: 80,
  preview_url: 'https://example.com/preview.mp3',
};

const mockTrackNoArtists: Track = {
  ...mockTrack,
  id: 'track456',
  name: 'Solo Track',
  artists: [],
};

const mockTrackNoAlbumImage: Track = {
  ...mockTrack,
  id: 'track789',
  name: 'Track Without Album Art',
  album: mockAlbumWithoutImage,
};


describe('TrackCard', () => {
  test('givenTrackCard_whenRenderedWithAlbumImageAndShowAlbumTrue_thenDisplaysCorrectInfoAndImage', () => {
    // given
    render(<TrackCard track={mockTrack} index={0} showAlbum={true} />);

    // then
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByAltText('track_image')).toHaveAttribute('src', mockAlbumWithImage.images[0].url);
    expect(screen.getByText('Awesome Song')).toBeInTheDocument();
    expect(screen.getByText('Test Artist')).toBeInTheDocument();
    expect(screen.getByText('Test Album')).toBeInTheDocument();
    expect(screen.getByText(formatDuration(mockTrack.duration_ms))).toBeInTheDocument();
    expect(screen.queryByTestId('play-icon')).not.toBeInTheDocument();
  });

  test('givenTrackCard_whenRenderedWithoutAlbumImage_thenDisplaysPlaceholderIcon', () => {
    // given
    render(<TrackCard track={mockTrackNoAlbumImage} index={1} showAlbum={true} />);

    // then
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.queryByAltText('track_image')).not.toBeInTheDocument();
    expect(screen.getByTestId('play-icon')).toBeInTheDocument();
    expect(screen.getByText('Track Without Album Art')).toBeInTheDocument();
    expect(screen.getByText('Test Artist')).toBeInTheDocument();
    expect(screen.getByText('Another Album')).toBeInTheDocument();
  });

  test('givenTrackCard_whenRendered_thenDisplaysFormattedDurationCorrectly', () => {
    // given
    const trackWithVariousDuration: Track = {
      ...mockTrack,
      duration_ms: 65000,
      name: 'Short Song',
    };
    render(<TrackCard track={trackWithVariousDuration} index={3} />);

    // then
    expect(screen.getByText('1:05')).toBeInTheDocument();
    expect(screen.getByText('Short Song')).toBeInTheDocument();
  });

  test('givenTrackCardWithNoArtists_whenRendered_thenDisplaysUnknownArtist', () => {
    // given
    render(<TrackCard track={mockTrackNoArtists} index={4} />);

    // then
    expect(screen.getByText('5')).toBeInTheDocument();
    expect(screen.getByText('Solo Track')).toBeInTheDocument();
    expect(screen.getByText('Unknown')).toBeInTheDocument();
  });
});
