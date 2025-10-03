import React from 'react';
import { Navigate } from 'react-router-dom';

export const LoginPage: React.FC = () => {
  // Redirect to the unified auth page
  return <Navigate to="/auth?mode=login" replace />;
};

export default LoginPage;