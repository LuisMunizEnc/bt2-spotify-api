import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../../components/ui/Button';

jest.mock('../../components/ui/LoadingSpinner', () => ({
  LoadingSpinner: ({ size, className }: { size: string; className: string }) => (
    <div data-testid="loading-spinner" className={`${className} size-${size}`}></div>
  ),
}));

describe('Button', () => {
  test('givenButton_whenRender_thenDisplayCorrectly', () => {
    // given
    render(<Button>Click Me</Button>);
    // then
    expect(screen.getByText('Click Me')).toBeInTheDocument();
  });

  test('givenButtonWithClickHandler_whenClicked_thenHandlerIsCalled', () => {
    // given
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Test Button</Button>);

    // when
    fireEvent.click(screen.getByText('Test Button'));

    // then
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  test('givenButtonWithSizeSm_whenRendered_thenAppliesSmClasses', () => {
    // given
    render(<Button size="sm">Small Button</Button>);

    // when
    const button = screen.getByText('Small Button');

    // then
    expect(button).toHaveClass('px-3 py-1.5 text-sm');
  });

  test('givenButtonWithoutSize_whenRendered_thenAppliesMdClassesByDefault', () => {
    // given
    render(<Button>Medium Button</Button>);

    // when
    const button = screen.getByText('Medium Button');

    // then
    expect(button).toHaveClass('px-4 py-2 text-base');
  });

  test('givenButtonWithSizeLg_whenRendered_thenAppliesLgClasses', () => {
    // given
    render(<Button size="lg">Large Button</Button>);

    // when
    const button = screen.getByText('Large Button');

    // then
    expect(button).toHaveClass('px-6 py-3 text-lg');
  });

  test('givenButtonWithLoadingTrue_whenRendered_thenDisplaysSpinnerAndIsDisabled', () => {
    // given
    render(<Button loading>Loading...</Button>);

    // when
    const button = screen.getByRole('button');

    // then
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
    expect(button).toBeDisabled();
    expect(button).toHaveClass('opacity-90 cursor-not-allowed');
  });

  test('givenButtonWithLoadingTrue_whenClicked_thenHandlerIsNotCalled', () => {
    // given
    const handleClick = jest.fn();
    render(<Button loading onClick={handleClick}>Loading Button</Button>);

    // when
    const button = screen.getByRole('button');
    fireEvent.click(button);

    // then
    expect(handleClick).not.toHaveBeenCalled();
  });

  test('givenButtonWithDisabledTrue_whenRendered_thenIsDisabledAndHandlerIsNotCalled', () => {
    // given
    const handleClick = jest.fn();
    render(<Button disabled onClick={handleClick}>Disabled Button</Button>);

    // when
    const button = screen.getByRole('button');
    fireEvent.click(button);

    // then
    expect(button).toBeDisabled();
    expect(button).toHaveClass('opacity-90 cursor-not-allowed');
    expect(handleClick).not.toHaveBeenCalled();
  });

  test('givenButtonWithDisabledAndLoadingTrue_whenRendered_thenPrioritizesLoadingAndIsDisabled', () => {
    // given
    const handleClick = jest.fn();
    render(<Button disabled loading onClick={handleClick}>Disabled Loading Button</Button>);

    // when
    const button = screen.getByRole('button');
    fireEvent.click(button);

    // then
    expect(button).toBeDisabled();
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(screen.queryByText('Disabled Loading Button')).not.toBeInTheDocument();
    expect(handleClick).not.toHaveBeenCalled();
  });

  test('givenButtonWithAdditionalClassName_whenRendered_thenAppliesAdditionalClass', () => {
    // given
    render(<Button className="custom-class">Custom Button</Button>);

    // when
    const button = screen.getByText('Custom Button');

    // then
    expect(button).toHaveClass('custom-class');
  });
});
