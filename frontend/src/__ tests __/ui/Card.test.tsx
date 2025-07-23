import React from 'react';
import { render, screen } from '@testing-library/react';
import { Card } from '../../components/ui/Card';

describe('Card', () => {
  test('givenCard_whenRenderedWithChildren_thenDisplaysChildren', () => {
    // given
    const cardContent = 'This is inside the card';
    render(<Card><div>{cardContent}</div></Card>);

    // then
    expect(screen.getByText(cardContent)).toBeInTheDocument();
  });

  test('givenCardWithAdditionalClassName_whenRendered_thenAppliesAdditionalClass', () => {
    // given
    const customClass = 'my-custom-card-style';
    render(<Card className={customClass}>Card with Custom Class</Card>);

    // when
    const cardElement = screen.getByText('Card with Custom Class').closest('div');

    // then
    expect(cardElement).toBeInTheDocument();
    expect(cardElement).toHaveClass(customClass);
  });

  test('givenCard_whenRenderedWithoutClassName_thenAppliesDefaultClasses', () => {
    // given
    render(<Card>Default Card</Card>);

    // when
    const cardElement = screen.getByText('Default Card').closest('div');

    // then
    expect(cardElement).toBeInTheDocument();
    expect(cardElement).toHaveClass('bg-gray-900', 'rounded-lg', 'p-6', 'shadow-lg', 'border', 'border-gray-800');
  });
});
