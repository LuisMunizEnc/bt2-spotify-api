import React from 'react';
import { render, screen } from '@testing-library/react';
import { LoadingSpinner } from '../../components/ui/LoadingSpinner';

jest.mock('lucide-react', () => ({
  Loader2: ({ className }: { className: string }) => (
    <svg data-testid="mock-loader2" className={className} />
  ),
}));

describe('LoadingSpinner', () => {
  test('givenLoadingSpinner_whenRendered_thenDisplaysSpinner', () => {
    // given
    render(<LoadingSpinner />);

    // then
    expect(screen.getByTestId('mock-loader2')).toBeInTheDocument();
  });

  test('givenLoadingSpinnerWithSizeSm_whenRendered_thenAppliesSmClasses', () => {
    // given
    render(<LoadingSpinner size="sm" />);

    // when
    const spinner = screen.getByTestId('mock-loader2');

    // then
    expect(spinner).toHaveClass('h-4', 'w-4');
  });

  test('givenLoadingSpinnerWithoutSize_whenRendered_thenAppliesMdClassesByDefault', () => {
    // given
    render(<LoadingSpinner />);

    // when
    const spinner = screen.getByTestId('mock-loader2');

    // then
    expect(spinner).toHaveClass('h-6', 'w-6');
  });

  test('givenLoadingSpinnerWithSizeLg_whenRendered_thenAppliesLgClasses', () => {
    // given
    render(<LoadingSpinner size="lg" />);

    // when
    const spinner = screen.getByTestId('mock-loader2');

    // then
    expect(spinner).toHaveClass('h-8', 'w-8');
  });

  test('givenLoadingSpinnerWithAdditionalClassName_whenRendered_thenAppliesAdditionalClass', () => {
    // given
    const customClass = 'extra-spin-style';
    render(<LoadingSpinner className={customClass} />);

    // when
    const spinner = screen.getByTestId('mock-loader2');

    // then
    expect(spinner).toHaveClass(customClass);
  });

  test('givenLoadingSpinner_whenRendered_thenAppliesAnimateSpinAndTextColor', () => {
    // given
    render(<LoadingSpinner />);

    // when
    const spinner = screen.getByTestId('mock-loader2');

    // then
    expect(spinner).toHaveClass('animate-spin', 'text-white');
  });
});
