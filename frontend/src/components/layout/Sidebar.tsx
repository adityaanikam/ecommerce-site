import React from 'react';
import { cn } from '@/utils/cn';
import { X } from 'lucide-react';
import { Button } from '@/components/ui';

export interface SidebarProps extends React.HTMLAttributes<HTMLDivElement> {
  isOpen: boolean;
  onClose: () => void;
  position?: 'left' | 'right';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  title?: string;
  children: React.ReactNode;
}

const sizeClasses = {
  sm: 'w-64',
  md: 'w-80',
  lg: 'w-96',
  xl: 'w-[28rem]',
};

export const Sidebar: React.FC<SidebarProps> = ({
  className,
  isOpen,
  onClose,
  position = 'right',
  size = 'md',
  title,
  children,
  ...props
}) => {
  React.useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }

    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  React.useEffect(() => {
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Sidebar */}
      <div
        className={cn(
          'fixed top-0 z-50 h-full bg-white dark:bg-secondary-900 shadow-xl transition-transform duration-300 ease-in-out',
          position === 'left' ? 'left-0' : 'right-0',
          sizeClasses[size],
          className
        )}
        {...props}
      >
        <div className="flex h-full flex-col">
          {/* Header */}
          {(title || onClose) && (
            <div className="flex items-center justify-between border-b border-secondary-200 dark:border-secondary-700 px-6 py-4">
              {title && (
                <h2 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                  {title}
                </h2>
              )}
              {onClose && (
                <Button
                  variant="ghost"
                  size="icon-sm"
                  onClick={onClose}
                  className="text-secondary-500 hover:text-secondary-700 dark:text-secondary-400 dark:hover:text-secondary-200"
                >
                  <X className="h-4 w-4" />
                </Button>
              )}
            </div>
          )}
          
          {/* Content */}
          <div className="flex-1 overflow-y-auto p-6">
            {children}
          </div>
        </div>
      </div>
    </>
  );
};
