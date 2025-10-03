import React from 'react';
import { Search, X } from 'lucide-react';
import { Input, Button, Card, CardContent, Badge } from '@/components';
import { useQuery } from '@tanstack/react-query';
import { categoryService } from '@/services/categoryService';
import { cn } from '@/utils/cn';

interface SearchFiltersProps {
  category?: string;
  subcategory?: string;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  onCategoryChange: (category: string) => void;
  onSubcategoryChange: (subcategory: string) => void;
  onPriceChange: (min?: number, max?: number) => void;
  onSearchChange: (search: string) => void;
  onClearFilters: () => void;
  className?: string;
}

const priceRanges = [
  { label: 'Under $50', min: 0, max: 50 },
  { label: '$50 - $100', min: 50, max: 100 },
  { label: '$100 - $200', min: 100, max: 200 },
  { label: '$200 - $500', min: 200, max: 500 },
  { label: 'Over $500', min: 500, max: undefined },
];

export const SearchFilters: React.FC<SearchFiltersProps> = ({
  category,
  subcategory,
  minPrice,
  maxPrice,
  search = '',
  onCategoryChange,
  onSubcategoryChange,
  onPriceChange,
  onSearchChange,
  onClearFilters,
  className,
}) => {
  const [localSearch, setLocalSearch] = React.useState(search);
  const searchTimeout = React.useRef<NodeJS.Timeout>();

  // Fetch categories
  const { data: categories, isLoading, error } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getCategories(),
  });

  // Get current category object
  const currentCategory = categories?.find(c => c.slug === category);

  // Handle debounced search
  const handleSearchChange = (value: string) => {
    setLocalSearch(value);
    if (searchTimeout.current) {
      clearTimeout(searchTimeout.current);
    }
    searchTimeout.current = setTimeout(() => {
      onSearchChange(value);
    }, 300);
  };

  // Count active filters
  const activeFiltersCount = [
    category,
    subcategory,
    minPrice,
    maxPrice,
    search
  ].filter(Boolean).length;

  return (
    <Card className={cn('overflow-hidden dark:bg-secondary-800 dark:border-secondary-700', className)}>
      <CardContent className="p-6">
        {/* Search */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-2">
            Search Products
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
            <Input
              value={localSearch}
              onChange={(e) => handleSearchChange(e.target.value)}
              placeholder="Search products..."
              className="pl-10 dark:bg-secondary-700 dark:border-secondary-600 dark:text-secondary-100 dark:placeholder-secondary-400"
            />
          </div>
        </div>

        {/* Active Filters */}
        {activeFiltersCount > 0 && (
          <div className="flex flex-wrap gap-2 mb-6 pb-6 border-b border-secondary-200 dark:border-secondary-700">
            {category && (
              <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                {categories.find(c => c.slug === category)?.name}
                <button
                  onClick={() => onCategoryChange('')}
                  className="ml-1 hover:text-error-600"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            )}
            {subcategory && currentCategory && (
              <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                {currentCategory.subcategories.find(s => s.slug === subcategory)?.name}
                <button
                  onClick={() => onSubcategoryChange('')}
                  className="ml-1 hover:text-error-600"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            )}
            {(minPrice !== undefined || maxPrice !== undefined) && (
              <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                Price: ${minPrice || 0} - ${maxPrice || '∞'}
                <button
                  onClick={() => onPriceChange(undefined, undefined)}
                  className="ml-1 hover:text-error-600"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            )}
            {search && (
              <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                Search: {search}
                <button
                  onClick={() => {
                    setLocalSearch('');
                    onSearchChange('');
                  }}
                  className="ml-1 hover:text-error-600"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            )}
            <Button
              variant="ghost"
              size="sm"
              onClick={onClearFilters}
              className="text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300"
            >
              Clear All
            </Button>
          </div>
        )}

        {/* Categories */}
        <div className="mb-6">
          <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
            Categories
          </h4>
          <div className="space-y-3">
            {isLoading ? (
              // Loading skeleton
              Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="h-6 bg-secondary-200 dark:bg-secondary-700 rounded w-3/4 mb-2" />
                  <div className="h-4 bg-secondary-200 dark:bg-secondary-700 rounded w-1/2" />
                </div>
              ))
            ) : error ? (
              <div className="text-center py-4">
                <p className="text-secondary-600 dark:text-secondary-400">
                  Failed to load categories
                </p>
                <Button onClick={() => window.location.reload()} className="mt-2">
                  Retry
                </Button>
              </div>
            ) : categories ? categories.map((cat) => (
              <div key={cat.slug}>
                <div className="flex items-center justify-between w-full">
                  <div className="custom-radio flex-1">
                    <input
                      type="radio"
                      id={`category-${cat.slug}`}
                      name="category"
                      checked={category === cat.slug}
                      onChange={() => onCategoryChange(cat.slug)}
                    />
                    <label htmlFor={`category-${cat.slug}`} className="custom-radio-label">
                      {cat.name}
                    </label>
                  </div>
                  <Badge variant="secondary" className="text-xs dark:bg-secondary-700 dark:text-secondary-100 ml-2">
                    {cat.subcategories.reduce((acc, sub) => acc + sub.count, 0)}
                  </Badge>
                </div>

                {/* Subcategories */}
                {category === cat.slug && (
                  <div className="ml-6 mt-2 space-y-2">
                    {cat.subcategories.map((sub) => (
                      <div key={sub.slug} className="flex items-center justify-between w-full">
                        <div className="custom-radio flex-1">
                          <input
                            type="radio"
                            id={`subcategory-${sub.slug}`}
                            name="subcategory"
                            checked={subcategory === sub.slug}
                            onChange={() => onSubcategoryChange(sub.slug)}
                          />
                          <label htmlFor={`subcategory-${sub.slug}`} className="custom-radio-label">
                            {sub.name}
                          </label>
                        </div>
                        <Badge variant="secondary" className="text-xs dark:bg-secondary-700 dark:text-secondary-100 ml-2">
                          {sub.count}
                        </Badge>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )) : null}
          </div>
        </div>

        {/* Price Ranges */}
        <div className="mb-6">
          <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
            Price Range
          </h4>
          <div className="space-y-3">
            {priceRanges.map((range) => (
              <div key={range.label} className="custom-radio">
                <input
                  type="radio"
                  id={`price-${range.label}`}
                  name="priceRange"
                  checked={minPrice === range.min && maxPrice === range.max}
                  onChange={() => onPriceChange(range.min, range.max)}
                />
                <label htmlFor={`price-${range.label}`} className="custom-radio-label">
                  {range.label}
                </label>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};