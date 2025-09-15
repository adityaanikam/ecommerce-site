import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';
import { cn } from '@/utils/cn';

export interface DropdownOption {
  value: string;
  label: string;
  disabled?: boolean;
  icon?: React.ReactNode;
}

export interface DropdownProps {
  options: DropdownOption[];
  value?: string;
  placeholder?: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'outline';
}

export const Dropdown: React.FC<DropdownProps> = ({
  options,
  value,
  placeholder = 'Select an option',
  onChange,
  disabled = false,
  className,
  size = 'md',
  variant = 'default',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const sizeClasses = {
    sm: 'h-8 px-3 text-xs',
    md: 'h-10 px-4 text-sm',
    lg: 'h-12 px-4 text-base',
  };

  const variantClasses = {
    default: 'bg-white border-secondary-300 hover:border-secondary-400 focus:border-primary-500',
    outline: 'bg-transparent border-secondary-300 hover:border-secondary-400 focus:border-primary-500',
  };

  const selectedOption = options.find(option => option.value === value);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleOptionClick = (optionValue: string) => {
    if (!disabled) {
      onChange(optionValue);
      setIsOpen(false);
    }
  };

  return (
    <div ref={dropdownRef} className={cn('relative', className)}>
      <button
        type="button"
        className={cn(
          'w-full flex items-center justify-between rounded-lg border transition-colors focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2',
          sizeClasses[size],
          variantClasses[variant],
          disabled && 'opacity-50 cursor-not-allowed',
          isOpen && 'ring-2 ring-primary-500 ring-offset-2'
        )}
        onClick={() => !disabled && setIsOpen(!isOpen)}
        disabled={disabled}
      >
        <span className={cn(
          'truncate',
          !selectedOption && 'text-secondary-500'
        )}>
          {selectedOption ? (
            <div className="flex items-center space-x-2">
              {selectedOption.icon && <span className="flex-shrink-0">{selectedOption.icon}</span>}
              <span>{selectedOption.label}</span>
            </div>
          ) : (
            placeholder
          )}
        </span>
        <ChevronDown className={cn(
          'h-4 w-4 text-secondary-400 transition-transform duration-200',
          isOpen && 'rotate-180'
        )} />
      </button>

      {isOpen && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-secondary-200 rounded-lg shadow-lg max-h-60 overflow-auto">
          {options.map((option) => (
            <button
              key={option.value}
              type="button"
              className={cn(
                'w-full flex items-center justify-between px-4 py-2 text-left hover:bg-secondary-50 transition-colors',
                option.disabled && 'opacity-50 cursor-not-allowed',
                option.value === value && 'bg-primary-50 text-primary-600'
              )}
              onClick={() => handleOptionClick(option.value)}
              disabled={option.disabled}
            >
              <div className="flex items-center space-x-2">
                {option.icon && <span className="flex-shrink-0">{option.icon}</span>}
                <span>{option.label}</span>
              </div>
              {option.value === value && (
                <Check className="h-4 w-4 text-primary-600" />
              )}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};
