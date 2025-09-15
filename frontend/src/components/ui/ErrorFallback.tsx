import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { Button, Card, CardContent, CardHeader, CardTitle } from '@/components/ui';

interface ErrorFallbackProps {
  error: Error;
  resetErrorBoundary: () => void;
}

export const ErrorFallback: React.FC<ErrorFallbackProps> = ({ error, resetErrorBoundary }) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-secondary-50 p-4">
      <Card className="max-w-md w-full">
        <CardHeader className="text-center">
          <div className="w-12 h-12 bg-error-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <AlertTriangle className="h-6 w-6 text-error-600" />
          </div>
          <CardTitle className="text-error-600">Something went wrong</CardTitle>
        </CardHeader>
        <CardContent className="text-center">
          <p className="text-secondary-600 mb-4">
            We're sorry, but something unexpected happened. Please try again.
          </p>
          
          {import.meta.env.DEV && (
            <details className="mb-4 text-left">
              <summary className="cursor-pointer text-sm font-medium text-secondary-700 mb-2">
                Error Details (Development)
              </summary>
              <pre className="text-xs text-error-600 bg-error-50 p-2 rounded overflow-auto">
                {error.message}
                {error.stack && (
                  <>
                    {'\n\n'}
                    {error.stack}
                  </>
                )}
              </pre>
            </details>
          )}
          
          <div className="flex flex-col sm:flex-row gap-2">
            <Button onClick={resetErrorBoundary} className="flex-1">
              <RefreshCw className="mr-2 h-4 w-4" />
              Try Again
            </Button>
            <Button
              variant="outline"
              onClick={() => window.location.href = '/'}
              className="flex-1"
            >
              Go Home
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
