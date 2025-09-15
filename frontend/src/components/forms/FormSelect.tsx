import React from 'react';
import { Dropdown, DropdownOption } from '@/components/ui';
import { cn } from '@/utils/cn';

export interface FormSelectProps {
  label?: string;
  error?: string;
  helperText?: string;
  containerClassName?: string;
  required?: boolean;
  success?: boolean;
  options: DropdownOption[];
  value?: string;
  placeholder?: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'outline';
}

export const FormSelect: React.FC<FormSelectProps> = ({
  label,
  error,
  helperText,
  containerClassName,
  required = false,
  success = false,
  options,
  value,
  placeholder = 'Select an option',
  onChange,
  disabled = false,
  size = 'md',
  variant = 'default',
}) => {
  const selectId = `select-${Math.random().toString(36).substr(2, 9)}`;

  return (
    <div className={cn('w-full', containerClassName)}>
      {label && (
        <label
          htmlFor={selectId}
          className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-1"
        >
          {label}
          {required && <span className="text-error-500 ml-1">*</span>}
        </label>
      )}
      
      <Dropdown
        options={options}
        value={value}
        placeholder={placeholder}
        onChange={onChange}
        disabled={disabled}
        size={size}
        variant={variant}
        className={cn(
          error && 'border-error-500 focus-within:ring-error-500',
          success && 'border-success-500 focus-within:ring-success-500'
        )}
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
