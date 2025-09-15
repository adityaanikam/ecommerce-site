import React from 'react';
import { useParams } from 'react-router-dom';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-4">Product Detail</h1>
      <p className="text-secondary-600">Product ID: {id}</p>
      <p className="mt-4">This page will show detailed product information, images, reviews, and add to cart functionality.</p>
    </div>
  );
};
