import React, { useEffect } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';
import { Button } from '@/components/ui';
import { cn } from '@/utils/cn';

export interface ToastProps {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  isVisible: boolean;
  isRemoving: boolean;
  onRemove: (id: string) => void;
}

const Toast: React.FC<ToastProps> = ({
  id,
  type,
  title,
  message,
  duration = 5000,
  action,
  isVisible,
  isRemoving,
  onRemove,
}) => {
  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        onRemove(id);
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [id, duration, onRemove]);

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <CheckCircle className="h-5 w-5 text-success-600" />;
      case 'error':
        return <AlertCircle className="h-5 w-5 text-error-600" />;
      case 'warning':
        return <AlertTriangle className="h-5 w-5 text-warning-600" />;
      case 'info':
        return <Info className="h-5 w-5 text-info-600" />;
      default:
        return <Info className="h-5 w-5 text-info-600" />;
    }
  };

  const getBackgroundColor = () => {
    switch (type) {
      case 'success':
        return 'bg-success-50 border-success-200 dark:bg-success-900/20 dark:border-success-800';
      case 'error':
        return 'bg-error-50 border-error-200 dark:bg-error-900/20 dark:border-error-800';
      case 'warning':
        return 'bg-warning-50 border-warning-200 dark:bg-warning-900/20 dark:border-warning-800';
      case 'info':
        return 'bg-info-50 border-info-200 dark:bg-info-900/20 dark:border-info-800';
      default:
        return 'bg-info-50 border-info-200 dark:bg-info-900/20 dark:border-info-800';
    }
  };

  return (
    <div
      className={cn(
        'relative w-full max-w-sm bg-white dark:bg-secondary-800 border rounded-lg shadow-lg p-4 transition-all duration-300 ease-in-out',
        getBackgroundColor(),
        isVisible && !isRemoving
          ? 'opacity-100 translate-x-0 scale-100'
          : 'opacity-0 translate-x-full scale-95',
        isRemoving && 'opacity-0 translate-x-full scale-95'
      )}
      role="alert"
      aria-live="polite"
    >
      <div className="flex items-start">
        <div className="flex-shrink-0">
          {getIcon()}
        </div>
        
        <div className="ml-3 flex-1">
          <h4 className="text-sm font-medium text-secondary-900 dark:text-secondary-100">
            {title}
          </h4>
          <p className="mt-1 text-sm text-secondary-600 dark:text-secondary-400">
            {message}
          </p>
          
          {action && (
            <div className="mt-3">
              <Button
                size="sm"
                variant="outline"
                onClick={action.onClick}
                className="text-xs"
              >
                {action.label}
              </Button>
            </div>
          )}
        </div>
        
        <div className="ml-4 flex-shrink-0">
          <Button
            variant="ghost"
            size="icon-sm"
            onClick={() => onRemove(id)}
            className="text-secondary-400 hover:text-secondary-600 dark:text-secondary-500 dark:hover:text-secondary-300"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
};

export default Toast;
