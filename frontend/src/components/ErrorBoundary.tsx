import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw, Home, Bug } from 'lucide-react';
import { Button, Card, CardContent } from '@/components/ui';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showDetails?: boolean;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo,
    });

    // Log error to console in development
    if (import.meta.env.DEV) {
      console.error('ErrorBoundary caught an error:', error.message);
      console.error('Error stack:', error.stack);
      if (errorInfo) {
        console.error('Component stack:', errorInfo.componentStack);
      }
    }

    // Call custom error handler if provided
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Log error to external service in production
    if (import.meta.env.PROD) {
      // Example: log to error reporting service
      // errorReportingService.logError(error, errorInfo);
    }
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  handleReload = () => {
    window.location.reload();
  };

  handleGoHome = () => {
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      // Custom fallback UI
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900 flex items-center justify-center p-4">
          <Card className="w-full max-w-2xl">
            <CardContent className="p-8 text-center">
              <div className="w-16 h-16 bg-error-100 dark:bg-error-900 rounded-full flex items-center justify-center mx-auto mb-6">
                <AlertTriangle className="h-8 w-8 text-error-600 dark:text-error-400" />
              </div>

              <h1 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
                Oops! Something went wrong
              </h1>

              <p className="text-secondary-600 dark:text-secondary-400 mb-8">
                We're sorry, but something unexpected happened. Our team has been notified and is working to fix the issue.
              </p>

              <div className="flex flex-col sm:flex-row gap-4 justify-center mb-8">
                <Button onClick={this.handleRetry} variant="outline">
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Try Again
                </Button>
                <Button onClick={this.handleReload} variant="outline">
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reload Page
                </Button>
                <Button onClick={this.handleGoHome}>
                  <Home className="h-4 w-4 mr-2" />
                  Go Home
                </Button>
              </div>

              {this.props.showDetails && this.state.error && (
                <details className="text-left bg-secondary-100 dark:bg-secondary-800 rounded-lg p-4">
                  <summary className="cursor-pointer font-medium text-secondary-900 dark:text-secondary-100 mb-2">
                    <Bug className="h-4 w-4 inline mr-2" />
                    Error Details
                  </summary>
                  <div className="space-y-2 text-sm">
                    <div>
                      <strong>Error:</strong> {this.state.error.message}
                    </div>
                    <div>
                      <strong>Stack:</strong>
                      <pre className="mt-1 p-2 bg-secondary-200 dark:bg-secondary-700 rounded text-xs overflow-auto">
                        {this.state.error.stack}
                      </pre>
                    </div>
                    {this.state.errorInfo && (
                      <div>
                        <strong>Component Stack:</strong>
                        <pre className="mt-1 p-2 bg-secondary-200 dark:bg-secondary-700 rounded text-xs overflow-auto">
                          {this.state.errorInfo.componentStack}
                        </pre>
                      </div>
                    )}
                  </div>
                </details>
              )}
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

// Hook-based error boundary for functional components
export const useErrorHandler = () => {
  const handleError = React.useCallback((error: Error, errorInfo?: any) => {
    // Log error to console in development
    if (import.meta.env.DEV) {
      console.error('Error caught by useErrorHandler:', error.message);
      console.error('Error stack:', error.stack);
      if (errorInfo) {
        console.error('Error info:', JSON.stringify(errorInfo, null, 2));
      }
    }

    // Log error to external service in production
    if (import.meta.env.PROD) {
      // Example: log to error reporting service
      // errorReportingService.logError(error, errorInfo);
    }
  }, []);

  return handleError;
};

// Higher-order component for error boundaries
export const withErrorBoundary = <P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<Props, 'children'>
) => {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;

  return WrappedComponent;
};

// Async error boundary for handling async errors
export class AsyncErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo,
    });

    // Handle async errors
    if (error.name === 'ChunkLoadError' || error.message.includes('Loading chunk')) {
      // Handle chunk loading errors (common with code splitting)
      console.warn('Chunk loading error detected, reloading page...');
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    }

    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900 flex items-center justify-center p-4">
          <Card className="w-full max-w-md">
            <CardContent className="p-6 text-center">
              <div className="w-12 h-12 bg-warning-100 dark:bg-warning-900 rounded-full flex items-center justify-center mx-auto mb-4">
                <RefreshCw className="h-6 w-6 text-warning-600 dark:text-warning-400" />
              </div>

              <h2 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100 mb-2">
                Loading Error
              </h2>

              <p className="text-secondary-600 dark:text-secondary-400 mb-4">
                There was a problem loading this page. Reloading...
              </p>

              <Button onClick={() => window.location.reload()} size="sm">
                <RefreshCw className="h-4 w-4 mr-2" />
                Reload Now
              </Button>
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
