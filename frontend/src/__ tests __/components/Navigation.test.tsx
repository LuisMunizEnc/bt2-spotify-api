import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Navigation } from '../../components/Navigation';
import { User as UserType } from '../../types';

const mockNavigate = jest.fn();
const mockUseLocation = jest.fn();
jest.mock('react-router-dom', () => ({
    useNavigate: () => mockNavigate,
    useLocation: () => mockUseLocation(),
}));

jest.mock('lucide-react', () => ({
    Search: ({ className }: { className: string }) => (
        <svg data-testid="search-icon" className={className} />
    ),
    TrendingUp: ({ className }: { className: string }) => (
        <svg data-testid="trending-up-icon" className={className} />
    ),
    LogOut: ({ className }: { className: string }) => (
        <svg data-testid="logout-icon" className={className} />
    ),
    Music: ({ className }: { className: string }) => (
        <svg data-testid="music-icon" className={className} />
    ),
}));

jest.mock('../../components/ui/Button', () => ({
    Button: ({ children, onClick, className }: { children: React.ReactNode; onClick: () => void; className?: string }) => (
        <button onClick={onClick} className={className} data-testid="mock-button">
            {children}
        </button>
    ),
}));

const mockLogout = jest.fn();
const mockUser: UserType = {
    id: 'user123',
    display_name: 'Test User',
    email: 'test@example.com',
    images: [{ url: 'https://example.com/user-avatar.jpg', height: 100, width: 100 }],
};

jest.mock('../../context/AuthContext', () => ({
    useAuth: jest.fn(() => ({
        user: mockUser,
        token: 'mock-token',
        login: jest.fn(),
        logout: mockLogout,
        isAuthenticated: true,
        loading: false,
    })),
}));

describe('Navigation', () => {
    beforeEach(() => {
        mockNavigate.mockClear();
        mockLogout.mockClear();
        mockUseLocation.mockReturnValue({ pathname: '/search' });
    });

    test('givenNavigation_whenRenderedOnSearchPath_thenSearchLinkIsActiveAndUserInfoDisplayed', () => {
        // given
        mockUseLocation.mockReturnValue({ pathname: '/search' });
        render(<Navigation />);

        // then
        expect(screen.getByRole('button', { name: 'Search' })).toHaveClass('bg-green-600');
        expect(screen.getByRole('button', { name: 'Dashboard' })).toHaveClass('text-gray-400');
        expect(screen.getByText('Test User')).toBeInTheDocument();
        expect(screen.getByAltText('Test User')).toHaveAttribute('src', 'https://example.com/user-avatar.jpg');
        expect(screen.getByText('Logout')).toBeInTheDocument();
    });

    test('givenNavigation_whenRenderedOnDashboardPath_thenDashboardLinkIsActiveAndUserInfoHidden', () => {
        // given
        mockUseLocation.mockReturnValue({ pathname: '/dashboard' });
        render(<Navigation />);

        // then
        expect(screen.getByRole('button', { name: 'Dashboard' })).toHaveClass('bg-green-600');
        expect(screen.getByRole('button', { name: 'Search' })).toHaveClass('text-gray-400');
        expect(screen.queryByText('Test User')).not.toBeInTheDocument();
        expect(screen.queryByAltText('Test User')).not.toBeInTheDocument();
        expect(screen.getByText('Logout')).toBeInTheDocument();
    });

    test('givenNavigation_whenSearchLinkIsClicked_thenNavigatesToSearch', () => {
        // given
        mockUseLocation.mockReturnValue({ pathname: '/dashboard' });
        render(<Navigation />);

        // when
        fireEvent.click(screen.getByText('Search'));

        // then
        expect(mockNavigate).toHaveBeenCalledTimes(1);
        expect(mockNavigate).toHaveBeenCalledWith('/search');
    });

    test('givenNavigation_whenDashboardLinkIsClicked_thenNavigatesToDashboard', () => {
        // given
        mockUseLocation.mockReturnValue({ pathname: '/search' });
        render(<Navigation />);

        // when
        fireEvent.click(screen.getByText('Dashboard'));

        // then
        expect(mockNavigate).toHaveBeenCalledTimes(1);
        expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });

    test('givenNavigation_whenLogoutButtonClicked_thenLogoutIsCalled', () => {
        // given
        render(<Navigation />);
        const logoutButton = screen.getByText('Logout');

        // when
        fireEvent.click(logoutButton);

        // then
        expect(mockLogout).toHaveBeenCalledTimes(1);
    });

    test('givenNavigation_whenRendered_thenDisplaysAppTitle', () => {
        // given
        render(<Navigation />);

        // then
        expect(screen.getByText('BT2')).toBeInTheDocument();
        expect(screen.getByTestId('music-icon')).toBeInTheDocument();
    });
});
