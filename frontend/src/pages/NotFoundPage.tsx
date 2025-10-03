import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Button } from '@/components';
import { Home, ShoppingBag, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container className="min-h-[80vh] flex items-center justify-center py-12">
      <div className="text-center">
        <div className="mb-8">
          <h1 className="text-9xl font-bold text-primary-600 dark:text-primary-400 mb-4">
            404
          </h1>
          <h2 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
            Page Not Found
          </h2>
          <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-md mx-auto">
            The page you're looking for doesn't exist or has been moved.
          </p>
        </div>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <Button
            onClick={() => navigate(-1)}
            variant="outline"
            className="w-full sm:w-auto"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Go Back
          </Button>
          <Button
            onClick={() => navigate('/')}
            variant="outline"
            className="w-full sm:w-auto"
          >
            <Home className="w-4 h-4 mr-2" />
            Home
          </Button>
          <Button
            onClick={() => navigate('/products')}
            className="w-full sm:w-auto"
          >
            <ShoppingBag className="w-4 h-4 mr-2" />
            Browse Products
          </Button>
        </div>

        {/* Animated 404 illustration */}
        <div className="mt-12 relative">
          <div className="absolute inset-0 bg-gradient-to-t from-background to-transparent z-10" />
          <div className="w-full max-w-lg mx-auto aspect-[2/1] bg-secondary-100 dark:bg-secondary-800 rounded-lg overflow-hidden">
            <div className="w-full h-full bg-[url('/404-pattern.svg')] bg-repeat animate-float opacity-10" />
          </div>
        </div>
      </div>
    </Container>
  );
};

export default NotFoundPage;