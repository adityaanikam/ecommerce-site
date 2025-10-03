import React from 'react';
import { Search, X } from 'lucide-react';
import { Input, Button, Card, CardContent, Badge } from '@/components';

export const RadioButtonFixTest: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('fashion');
  const [selectedPrice, setSelectedPrice] = React.useState('under-50');
  const [search, setSearch] = React.useState('');

  const categories = [
    { value: 'electronics', label: 'Electronics', count: 50 },
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

  // Count active filters
  const activeFiltersCount = [
    selectedCategory,
    selectedPrice,
    search
  ].filter(Boolean).length;

  const clearAllFilters = () => {
    setSelectedCategory('');
    setSelectedPrice('');
    setSearch('');
  };

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
          Radio Button Fixes Test
        </h1>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Filter Sidebar */}
          <div className="lg:col-span-1">
            <Card className="overflow-hidden dark:bg-secondary-800 dark:border-secondary-700">
              <CardContent className="p-6">
                {/* Search */}
                <div className="mb-6">
                  <label className="block text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-2">
                    Search Products
                  </label>
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                    <Input
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      placeholder="Search products..."
                      className="pl-10 dark:bg-secondary-700 dark:border-secondary-600 dark:text-secondary-100 dark:placeholder-secondary-400"
                    />
                  </div>
                </div>

                {/* Active Filters - This should show the selected category and price */}
                {activeFiltersCount > 0 && (
                  <div className="flex flex-wrap gap-2 mb-6 pb-6 border-b border-secondary-200 dark:border-secondary-700">
                    {selectedCategory && (
                      <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                        {categories.find(c => c.value === selectedCategory)?.label}
                        <button
                          onClick={() => setSelectedCategory('')}
                          className="ml-1 hover:text-error-600"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    )}
                    {selectedPrice && (
                      <Badge variant="secondary" className="flex items-center gap-1 dark:bg-secondary-700 dark:text-secondary-100">
                        Price: {priceRanges.find(p => p.value === selectedPrice)?.label}
                        <button
                          onClick={() => setSelectedPrice('')}
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
                          onClick={() => setSearch('')}
                          className="ml-1 hover:text-error-600"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={clearAllFilters}
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
                    {categories.map((category) => (
                      <div key={category.value}>
                        <div className="flex items-center justify-between">
                          <div className="custom-radio">
                            <input
                              type="radio"
                              id={`category-${category.value}`}
                              name="category"
                              checked={selectedCategory === category.value}
                              onChange={() => setSelectedCategory(category.value)}
                            />
                            <label htmlFor={`category-${category.value}`} className="custom-radio-label">
                              {category.label}
                            </label>
                          </div>
                          <Badge variant="secondary" className="text-xs dark:bg-secondary-700 dark:text-secondary-100">
                            {category.count}
                          </Badge>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Price Range */}
                <div className="mb-6">
                  <h4 className="text-sm font-medium text-secondary-700 dark:text-secondary-300 mb-3">
                    Price Range
                  </h4>
                  <div className="space-y-3">
                    {priceRanges.map((range) => (
                      <div key={range.value} className="custom-radio">
                        <input
                          type="radio"
                          id={`price-${range.value}`}
                          name="priceRange"
                          checked={selectedPrice === range.value}
                          onChange={() => setSelectedPrice(range.value)}
                        />
                        <label htmlFor={`price-${range.value}`} className="custom-radio-label">
                          {range.label}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Results Area */}
          <div className="lg:col-span-2">
            <Card className="dark:bg-secondary-800 dark:border-secondary-700">
              <CardContent className="p-6">
                <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-white">
                  Fix Verification
                </h2>
                
                <div className="space-y-4">
                  <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                    <h3 className="font-medium text-green-900 dark:text-green-100 mb-2">
                      ✅ Fixed Issues:
                    </h3>
                    <ul className="text-sm text-green-800 dark:text-green-200 space-y-1">
                      <li>• <strong>Blue Filled Circles:</strong> Selected radio buttons now show proper blue filled circles</li>
                      <li>• <strong>Layout Fixed:</strong> No more overlapping in price range section (increased spacing)</li>
                      <li>• <strong>Filter Chips:</strong> Selected categories appear as filter chips below search bar</li>
                      <li>• <strong>Proper Spacing:</strong> All radio buttons have consistent spacing</li>
                    </ul>
                  </div>

                  <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <h3 className="font-medium text-blue-900 dark:text-blue-100 mb-2">
                      🎯 Expected Behavior:
                    </h3>
                    <ul className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                      <li>• <strong>Fashion</strong> should show as selected with blue border + blue inner circle</li>
                      <li>• <strong>Under $50</strong> should show as selected with blue border + blue inner circle</li>
                      <li>• <strong>Filter chips</strong> should appear below search bar showing "Fashion" and "Price: Under $50"</li>
                      <li>• <strong>No overlapping</strong> in the price range section</li>
                    </ul>
                  </div>

                  <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                    <h3 className="font-medium text-gray-900 dark:text-white mb-2">
                      Current Selection:
                    </h3>
                    <div className="text-sm text-gray-600 dark:text-gray-300 space-y-1">
                      <p><strong>Category:</strong> {selectedCategory ? categories.find(c => c.value === selectedCategory)?.label : 'None'}</p>
                      <p><strong>Price Range:</strong> {selectedPrice ? priceRanges.find(p => p.value === selectedPrice)?.label : 'None'}</p>
                      <p><strong>Search:</strong> {search || 'None'}</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};
