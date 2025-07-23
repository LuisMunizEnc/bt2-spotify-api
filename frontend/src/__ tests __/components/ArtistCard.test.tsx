import { render, screen, fireEvent } from '@testing-library/react';
import { ArtistCard } from '../../components/ArtistCard';
import { Artist, SpotifyImage } from '../../types';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
}));

jest.mock('lucide-react', () => ({
    User: ({ className }: { className: string }) => (
        <svg data-testid="user-icon" className={className} />
    ),
}));

const mockArtistWithImage: Artist = {
    id: 'artist789',
    name: 'Test Artist Name',
    images: [
        { url: 'https://example.com/artist-image.jpg', height: 300, width: 300 },
    ],
    external_urls: { spotify: 'https://spotify.com/artist/artist789' },
    followers: { href: '', total: 5000 },
};

const mockArtistWithoutImage: Artist = {
    id: 'artist101',
    name: 'No Image Artist',
    images: [],
    external_urls: { spotify: 'https://spotify.com/artist/artist101' },
    followers: { href: '', total: 100 },
};

describe('ArtistCard', () => {
    test('givenArtistCard_whenRenderedWithImage_thenDisplaysCorrectInfoAndImage', () => {
        // given
        render(<ArtistCard artist={mockArtistWithImage} />);

        // then
        expect(screen.getByText('Test Artist Name')).toBeInTheDocument();
        expect(screen.getByAltText('Test Artist Name')).toHaveAttribute('src', 'https://example.com/artist-image.jpg');
        expect(screen.queryByTestId('user-icon')).not.toBeInTheDocument();
    });

    test('givenArtistCardWithNoImage_whenRendered_thenDisplaysPlaceholderIcon', () => {
        // given
        render(<ArtistCard artist={mockArtistWithoutImage} />);

        // then
        expect(screen.getByText('No Image Artist')).toBeInTheDocument();
        expect(screen.getByTestId('user-icon')).toBeInTheDocument();
        expect(screen.queryByAltText('No Image Artist')).not.toBeInTheDocument(); 
    });

    test('givenArtistCard_whenClicked_thenNavigatesToArtistPage', () => {
        // given
        render(<ArtistCard artist={mockArtistWithImage} />);

        // when
        fireEvent.click(screen.getByText('Test Artist Name'));

        // then
        expect(mockNavigate).toHaveBeenCalledTimes(1);
        expect(mockNavigate).toHaveBeenCalledWith('/artist/artist789');
    });

    test('givenArtistCard_whenRendered_thenArtistNameIsDisplayed', () => {
        // given
        render(<ArtistCard artist={mockArtistWithImage} />);

        // then
        expect(screen.getByText('Test Artist Name')).toBeInTheDocument();
    });
});
