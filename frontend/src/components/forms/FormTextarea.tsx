import React from 'react';
import { cn } from '@/utils/cn';

export interface FormTextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  containerClassName?: string;
  required?: boolean;
  success?: boolean;
  resize?: 'none' | 'vertical' | 'horizontal' | 'both';
}

export const FormTextarea: React.FC<FormTextareaProps> = ({
  className,
  label,
  error,
  helperText,
  containerClassName,
  required = false,
  success = false,
  resize = 'vertical',
  id,
  ...props
}) => {
  const textareaId = id || `textarea-${Math.random().toString(36).substr(2, 9)}`;

  const resizeClasses = {
    none: 'resize-none',
    vertical: 'resize-y',
    horizontal: 'resize-x',
    both: 'resize',
  };

  return (
    <div className={cn('w-full', containerClassName)}>
      {label && (
        <label
          htmlFor={textareaId}
          className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1"
        >
          {label}
          {required && <span className="text-error-500 ml-1">*</span>}
        </label>
      )}
      
      <textarea
        id={textareaId}
        className={cn(
          'flex min-h-[80px] w-full rounded-lg border bg-white px-3 py-2 text-sm ring-offset-background placeholder:text-secondary-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 transition-colors',
          'border-secondary-300 focus-visible:ring-primary-500 focus-visible:border-primary-500',
          error && 'border-error-500 focus-visible:ring-error-500 focus-visible:border-error-500',
          success && 'border-success-500 focus-visible:ring-success-500 focus-visible:border-success-500',
          resizeClasses[resize],
          'dark:bg-secondary-900 dark:border-secondary-700 dark:text-secondary-100 dark:placeholder:text-secondary-400',
          className
        )}
        {...props}
      />
      
      {error && (
        <p className="mt-1 text-sm text-error-600 dark:text-error-400 flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {error}
        </p>
      )}
      
      {helperText && !error && (
        <p className="mt-1 text-sm text-secondary-500 dark:text-secondary-400">
          {helperText}
        </p>
      )}
      
      {success && !error && (
        <p className="mt-1 text-sm text-success-600 dark:text-success-400 flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
          {helperText || 'Valid'}
        </p>
      )}
    </div>
  );
};
