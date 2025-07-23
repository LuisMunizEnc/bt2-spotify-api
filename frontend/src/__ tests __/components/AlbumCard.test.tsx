import { render, screen, fireEvent } from '@testing-library/react';
import { AlbumCard } from '../../components/AlbumCard';
import { Album } from '../../types';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

jest.mock('lucide-react', () => ({
  Music: ({ className }: { className: string }) => (
    <svg data-testid="music-icon" className={className} />
  ),
  Calendar: ({ className }: { className: string }) => (
    <svg data-testid="calendar-icon" className={className} />
  ),
}));

const mockAlbum: Album = {
  id: 'album123',
  album_type: 'album',
  total_tracks: '10',
  external_url: { spotify: 'https://spotify.com/album/album123' },
  images: [
    { url: 'https://example.com/album-art.jpg', height: 640, width: 640 },
  ],
  name: 'Test Album Name',
  artists: [
    {
      id: 'artist456',
      name: 'Test Artist',
      images: [],
      external_urls: { spotify: 'https://spotify.com/artist/artist456' },
      followers: { href: '', total: 1000 },
    },
  ],
  release_date: '2023-01-15',
  track: [],
};

describe('AlbumCard', () => {
  test('givenAlbumCard_whenRenderedHorizontal_thenDisplaysCorrectInfoAndImage', () => {
    // given
    render(<AlbumCard album={mockAlbum} variant="horizontal" />);

    // then
    expect(screen.getByText('Test Album Name')).toBeInTheDocument();
    expect(screen.getByText('Test Artist')).toBeInTheDocument();
    expect(screen.getByText('2023')).toBeInTheDocument();
    expect(screen.getByAltText('Test Album Name')).toHaveAttribute('src', 'https://example.com/album-art.jpg');
    expect(screen.queryByTestId('music-icon')).not.toBeInTheDocument();
  });

  test('givenAlbumCardWithNoImage_whenRenderedHorizontal_thenDisplaysPlaceholderIcon', () => {
    // given
    const albumWithoutImage = { ...mockAlbum, images: [] };
    render(<AlbumCard album={albumWithoutImage} variant="horizontal" />);

    // then
    expect(screen.getByTestId('music-icon')).toBeInTheDocument();
    expect(screen.queryByAltText('Test Album Name')).not.toBeInTheDocument();
  });

  test('givenAlbumCard_whenClicked_thenNavigatesToAlbumPage', () => {
    // given
    render(<AlbumCard album={mockAlbum} />);

    // when
    fireEvent.click(screen.getByText('Test Album Name'));

    // then
    expect(mockNavigate).toHaveBeenCalledTimes(1);
    expect(mockNavigate).toHaveBeenCalledWith('/album/album123');
  });

  test('givenAlbumCard_whenRenderedVertical_thenDisplaysCorrectInfoAndImage', () => {
    // given
    render(<AlbumCard album={mockAlbum} variant="vertical" />);

    // then
    expect(screen.getByText('Test Album Name')).toBeInTheDocument();
    expect(screen.getByAltText('Test Album Name')).toHaveAttribute('src', 'https://example.com/album-art.jpg');
    expect(screen.queryByTestId('music-icon')).not.toBeInTheDocument();
  });
});
