import React from 'react';
import { cn } from '@/utils/cn';

interface CustomRadioButtonProps {
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
}

export const CustomRadioButton: React.FC<CustomRadioButtonProps> = ({
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
}) => {
  const sizeClass = {
    sm: 'custom-radio-sm',
    md: '',
    lg: 'custom-radio-lg',
  }[size];

  const variantClass = {
    primary: 'custom-radio-primary',
    success: 'custom-radio-success',
    warning: 'custom-radio-warning',
  }[variant];

  return (
    <div className={cn('custom-radio', sizeClass, variantClass, className)}>
      <input
        type="radio"
        id={id}
        name={name}
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
        disabled={disabled}
        className="custom-radio-input"
      />
      <label htmlFor={id} className="custom-radio-label">
        {label}
      </label>
    </div>
  );
};

// Example usage component
export const RadioButtonExample: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('electronics');
  const [selectedPrice, setSelectedPrice] = React.useState('under-50');

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
    <div className="p-6 bg-white dark:bg-gray-900 min-h-screen">
      <div className="max-w-md mx-auto">
        <h2 className="text-xl font-semibold mb-6 text-gray-900 dark:text-white">
          Product Filters
        </h2>
        
        {/* Categories Section */}
        <div className="mb-8">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
            Categories
          </h3>
          <div className="space-y-3">
            {categories.map((category) => (
              <div key={category.value} className="flex items-center justify-between">
                <CustomRadioButton
                  id={`category-${category.value}`}
                  name="category"
                  value={category.value}
                  checked={selectedCategory === category.value}
                  onChange={setSelectedCategory}
                  label={category.label}
                />
                <span className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-2 py-1 rounded-full">
                  {category.count}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Price Range Section */}
        <div className="mb-8">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
            Price Range
          </h3>
          <div className="space-y-3">
            {priceRanges.map((range) => (
              <CustomRadioButton
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

        {/* Size Variants Demo */}
        <div className="mb-8">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
            Size Variants
          </h3>
          <div className="space-y-3">
            <CustomRadioButton
              id="size-sm"
              name="size"
              value="sm"
              checked={false}
              onChange={() => {}}
              label="Small Radio"
              size="sm"
            />
            <CustomRadioButton
              id="size-md"
              name="size"
              value="md"
              checked={true}
              onChange={() => {}}
              label="Medium Radio (Default)"
              size="md"
            />
            <CustomRadioButton
              id="size-lg"
              name="size"
              value="lg"
              checked={false}
              onChange={() => {}}
              label="Large Radio"
              size="lg"
            />
          </div>
        </div>

        {/* Color Variants Demo */}
        <div className="mb-8">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
            Color Variants
          </h3>
          <div className="space-y-3">
            <CustomRadioButton
              id="color-primary"
              name="color"
              value="primary"
              checked={true}
              onChange={() => {}}
              label="Primary (Blue)"
              variant="primary"
            />
            <CustomRadioButton
              id="color-success"
              name="color"
              value="success"
              checked={false}
              onChange={() => {}}
              label="Success (Green)"
              variant="success"
            />
            <CustomRadioButton
              id="color-warning"
              name="color"
              value="warning"
              checked={false}
              onChange={() => {}}
              label="Warning (Orange)"
              variant="warning"
            />
          </div>
        </div>
      </div>
    </div>
  );
};
