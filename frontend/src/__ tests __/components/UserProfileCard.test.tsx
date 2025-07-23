import React from 'react';
import { render, screen } from '@testing-library/react';
import { UserProfileCard } from '../../components/UserProfileCard';
import { User, SpotifyImage } from '../../types';

jest.mock('lucide-react', () => ({
  User: ({ className }: { className: string }) => (
    <svg data-testid="user-icon" className={className} />
  ),
  MapPin: ({ className }: { className: string }) => (
    <svg data-testid="map-pin-icon" className={className} />
  ),
}));

const mockUserWithImageAndCountry: User = {
  id: 'user123',
  display_name: 'John Doe',
  email: 'john.doe@example.com',
  images: [{ url: 'https://example.com/profile.jpg', height: 200, width: 200 }],
  country: 'US',
  product: 'premium',
  externalURL: { spotify: 'https://spotify.com/user/john_doe' },
};

const mockUserWithoutImage: User = {
  id: 'user456',
  display_name: 'Jane Smith',
  email: 'jane.smith@example.com',
  images: [], 
  country: 'CA',
  product: 'free',
  externalURL: { spotify: 'https://spotify.com/user/jane_smith' },
};

const mockUserWithoutCountry: User = {
  id: 'user789',
  display_name: 'Guest User',
  email: 'guest@example.com',
  images: [{ url: 'https://example.com/guest.jpg', height: 200, width: 200 }],
  country: undefined,
  product: 'free',
  externalURL: { spotify: 'https://spotify.com/user/guest' },
};

describe('UserProfileCard', () => {
  test('givenUserProfileCard_whenRenderedWithImageAndCountry_thenDisplaysAllInfo', () => {
    // given
    render(<UserProfileCard user={mockUserWithImageAndCountry} />);

    // then
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
    expect(screen.getByAltText('John Doe')).toHaveAttribute('src', 'https://example.com/profile.jpg');
    expect(screen.getByText('US')).toBeInTheDocument();
    expect(screen.getByTestId('map-pin-icon')).toBeInTheDocument();
    expect(screen.queryByTestId('user-icon')).not.toBeInTheDocument();
  });

  test('givenUserProfileCard_whenRenderedWithoutImage_thenDisplaysPlaceholderIcon', () => {
    // given
    render(<UserProfileCard user={mockUserWithoutImage} />);

    // then
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@example.com')).toBeInTheDocument();
    expect(screen.queryByAltText('Jane Smith')).not.toBeInTheDocument();
    expect(screen.getByTestId('user-icon')).toBeInTheDocument();
    expect(screen.getByText('CA')).toBeInTheDocument();
  });

  test('givenUserProfileCard_whenRenderedWithoutCountry_thenCountryInfoIsNotDisplayed', () => {
    // given
    render(<UserProfileCard user={mockUserWithoutCountry} />);

    // then
    expect(screen.getByText('Guest User')).toBeInTheDocument();
    expect(screen.getByText('guest@example.com')).toBeInTheDocument();
    expect(screen.getByAltText('Guest User')).toBeInTheDocument();
    expect(screen.queryByText('undefined')).not.toBeInTheDocument(); 
    expect(screen.queryByTestId('map-pin-icon')).not.toBeInTheDocument();
  });

  test('givenUserProfileCard_whenRendered_thenDisplaysDisplayNameAndEmail', () => {
    // given
    render(<UserProfileCard user={mockUserWithImageAndCountry} />);

    // then
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
  });
});
