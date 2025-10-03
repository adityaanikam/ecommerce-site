import React from 'react';
import { Badge } from '@/components';

export const AlignmentTest: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('fashion');
  const [selectedPrice, setSelectedPrice] = React.useState('under-50');

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

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
          Alignment Test - Like Second Image
        </h1>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Filter Sidebar */}
          <div>
            <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
              <h2 className="text-lg font-semibold mb-6 text-gray-900 dark:text-white">
                Filter Sidebar - Proper Alignment
              </h2>
              
              {/* Categories with Badge (Like SearchFilters) */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                  Categories (With Badge)
                </h4>
                <div className="space-y-3">
                  {categories.map((category) => (
                    <div key={category.value}>
                      <div className="flex items-center justify-between w-full">
                        <div className="custom-radio flex-1">
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
                        <Badge variant="secondary" className="text-xs dark:bg-secondary-700 dark:text-secondary-100 ml-2">
                          {category.count}
                        </Badge>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Price Range (Simple) */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                  Price Range (Simple)
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
            </div>
          </div>

          {/* Results Area */}
          <div>
            <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
              <h2 className="text-lg font-semibold mb-4 text-gray-900 dark:text-white">
                Alignment Test Results
              </h2>
              
              <div className="space-y-4">
                <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <h3 className="font-medium text-green-900 dark:text-green-100 mb-2">
                    ✅ Expected Alignment (Like Second Image):
                  </h3>
                  <ul className="text-sm text-green-800 dark:text-green-200 space-y-1">
                    <li>• <strong>Radio buttons:</strong> Perfectly left-aligned vertically</li>
                    <li>• <strong>Category labels:</strong> Left-aligned with each other</li>
                    <li>• <strong>Count badges:</strong> Right-aligned in a clean column</li>
                    <li>• <strong>Price ranges:</strong> Left-aligned with each other</li>
                    <li>• <strong>Blue dots:</strong> Appear when selected (Fashion, Under $50)</li>
                  </ul>
                </div>

                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <h3 className="font-medium text-blue-900 dark:text-blue-100 mb-2">
                    🎯 Current Selection:
                  </h3>
                  <div className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                    <p><strong>Category:</strong> {selectedCategory ? categories.find(c => c.value === selectedCategory)?.label : 'None'}</p>
                    <p><strong>Price Range:</strong> {selectedPrice ? priceRanges.find(p => p.value === selectedPrice)?.label : 'None'}</p>
                  </div>
                </div>

                <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                  <h3 className="font-medium text-yellow-900 dark:text-yellow-100 mb-2">
                    🔧 Alignment Fixes Applied:
                  </h3>
                  <div className="text-sm text-yellow-800 dark:text-yellow-200 space-y-1">
                    <p>• Changed <code>display: inline-flex</code> to <code>display: flex</code></p>
                    <p>• Added <code>width: 100%</code> to radio containers</p>
                    <p>• Added <code>flex: 1</code> to labels for proper spacing</p>
                    <p>• Added <code>ml-2</code> to badges for proper spacing</p>
                    <p>• Consistent <code>space-y-3</code> for vertical spacing</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
