import React from 'react';
import { Navigate } from 'react-router-dom';

export const RegisterPage: React.FC = () => {
  // Redirect to the unified auth page
  return <Navigate to="/auth?mode=register" replace />;
};
