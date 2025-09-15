import React from 'react';
import { useParams } from 'react-router-dom';

export const OrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-4">Order Detail</h1>
      <p className="text-secondary-600">Order ID: {id}</p>
      <p className="mt-4">This page will show detailed order information, tracking, and status updates.</p>
    </div>
  );
};
