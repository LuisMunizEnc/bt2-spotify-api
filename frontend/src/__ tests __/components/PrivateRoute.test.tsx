import { render, screen } from '@testing-library/react';
import { PrivateRoute } from '../../components/PrivateRoute';

const mockNavigate = jest.fn(({ to, replace }) => (
  <div data-testid="navigate-mock" data-to={to} data-replace={replace ? 'true' : 'false'}></div>
));
jest.mock('react-router-dom', () => ({
  Navigate: (props: any) => mockNavigate(props),
}));

jest.mock('../../components/ui/LoadingSpinner', () => ({
  LoadingSpinner: ({ size }: { size: string }) => (
    <div data-testid="loading-spinner-mock" data-size={size}></div>
  ),
}));

const mockUseAuth = jest.fn();
jest.mock('../../context/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

describe('PrivateRoute', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    mockUseAuth.mockClear();
  });

  test('givenPrivateRoute_whenLoadingIsTrue_thenDisplaysLoadingSpinner', () => {
    // given
    mockUseAuth.mockReturnValue({ isAuthenticated: false, loading: true });
    render(
      <PrivateRoute>
        <div>Protected Content</div>
      </PrivateRoute>
    );

    // then
    expect(screen.getByTestId('loading-spinner-mock')).toBeInTheDocument();
    expect(screen.getByTestId('loading-spinner-mock')).toHaveAttribute('data-size', 'lg');
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    expect(screen.queryByTestId('navigate-mock')).not.toBeInTheDocument();
  });

  test('givenPrivateRoute_whenAuthenticatedIsTrue_thenDisplaysChildren', () => {
    // given
    mockUseAuth.mockReturnValue({ isAuthenticated: true, loading: false });
    render(
      <PrivateRoute>
        <div>Protected Content</div>
      </PrivateRoute>
    );

    // then
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(screen.queryByTestId('loading-spinner-mock')).not.toBeInTheDocument();
    expect(screen.queryByTestId('navigate-mock')).not.toBeInTheDocument();
  });

  test('givenPrivateRoute_whenAuthenticatedIsFalse_thenNavigatesToLogin', () => {
    // given
    mockUseAuth.mockReturnValue({ isAuthenticated: false, loading: false });
    render(
      <PrivateRoute>
        <div>Protected Content</div>
      </PrivateRoute>
    );

    // then
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    expect(screen.queryByTestId('loading-spinner-mock')).not.toBeInTheDocument();
    expect(screen.getByTestId('navigate-mock')).toBeInTheDocument();
    expect(screen.getByTestId('navigate-mock')).toHaveAttribute('data-to', '/login');
    expect(screen.getByTestId('navigate-mock')).toHaveAttribute('data-replace', 'true');
  });
});
