import React from 'react';
import { cn } from '@/utils/cn';

interface TailwindRadioButtonProps {
  id: string;
  name: string;
  value: string;
  checked: boolean;
  onChange: (value: string) => void;
  label: string;
  disabled?: boolean;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'primary' | 'success' | 'warning';
  className?: string;
  count?: number;
}

export const TailwindRadioButton: React.FC<TailwindRadioButtonProps> = ({
  id,
  name,
  value,
  checked,
  onChange,
  label,
  disabled = false,
  size = 'md',
  variant = 'primary',
  className,
  count,
}) => {
  const sizeClass = {
    sm: 'radio-custom-sm',
    md: '',
    lg: 'radio-custom-lg',
  }[size];

  const variantClass = {
    primary: 'radio-custom-primary',
    success: 'radio-custom-success',
    warning: 'radio-custom-warning',
  }[variant];

  return (
    <div className={cn('flex items-center justify-between', className)}>
      <div className={cn('radio-custom', sizeClass, variantClass)}>
        <input
          type="radio"
          id={id}
          name={name}
          value={value}
          checked={checked}
          onChange={() => onChange(value)}
          disabled={disabled}
        />
        <label htmlFor={id} className="radio-custom-label">
          {label}
        </label>
      </div>
      {count !== undefined && (
        <span className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-2 py-1 rounded-full">
          {count}
        </span>
      )}
    </div>
  );
};

// Example usage component for your filter sidebar
export const FilterSidebar: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('electronics');
  const [selectedPrice, setSelectedPrice] = React.useState('');

  const categories = [
    { value: 'electronics', label: 'Electronics', count: 5 },
    { value: 'fashion', label: 'Fashion', count: 30 },
    { value: 'sports', label: 'Sports', count: 24 },
    { value: 'home-garden', label: 'Home & Garden', count: 24 },
  ];

  const priceRanges = [
    { value: 'under-50', label: 'Under $50' },
    { value: '50-100', label: '$50 - $100' },
    { value: '100-200', label: '$100 - $200' },
    { value: '200-500', label: '$200 - $500' },
    { value: 'over-500', label: 'Over $500' },
  ];

  return (
    <div className="w-64 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-6">
      <h2 className="text-lg font-semibold mb-6 text-gray-900 dark:text-white">
        Filters
      </h2>
      
      {/* Categories Section */}
      <div className="mb-6">
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
          Categories
        </h3>
        <div className="space-y-3">
          {categories.map((category) => (
            <TailwindRadioButton
              key={category.value}
              id={`category-${category.value}`}
              name="category"
              value={category.value}
              checked={selectedCategory === category.value}
              onChange={setSelectedCategory}
              label={category.label}
              count={category.count}
            />
          ))}
        </div>
      </div>

      {/* Price Range Section */}
      <div className="mb-6">
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
          Price Range
        </h3>
        <div className="space-y-3">
          {priceRanges.map((range) => (
            <TailwindRadioButton
              key={range.value}
              id={`price-${range.value}`}
              name="priceRange"
              value={range.value}
              checked={selectedPrice === range.value}
              onChange={setSelectedPrice}
              label={range.label}
            />
          ))}
        </div>
      </div>

      {/* Clear Filters Button */}
      <button
        onClick={() => {
          setSelectedCategory('');
          setSelectedPrice('');
        }}
        className="w-full text-sm text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors"
      >
        Clear All
      </button>
    </div>
  );
};
