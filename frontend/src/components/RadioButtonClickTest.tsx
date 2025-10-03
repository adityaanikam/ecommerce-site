import React from 'react';

export const RadioButtonClickTest: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('');
  const [selectedPrice, setSelectedPrice] = React.useState('');

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
          Radio Button Click Test
        </h1>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Filter Sidebar */}
          <div>
            <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
              <h2 className="text-lg font-semibold mb-6 text-gray-900 dark:text-white">
                Test Radio Buttons
              </h2>
              
              {/* Categories */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
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
                        <span className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-2 py-1 rounded-full">
                          {category.count}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Price Range */}
              <div className="mb-6">
                <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
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
            </div>
          </div>

          {/* Results Area */}
          <div>
            <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
              <h2 className="text-lg font-semibold mb-4 text-gray-900 dark:text-white">
                Click Test Results
              </h2>
              
              <div className="space-y-4">
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <h3 className="font-medium text-blue-900 dark:text-blue-100 mb-2">
                    🎯 Click Test Instructions:
                  </h3>
                  <ul className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
                    <li>1. Click on any category radio button</li>
                    <li>2. Click on any price range radio button</li>
                    <li>3. Check if the blue dots appear and selections update</li>
                    <li>4. Try clicking the labels too (should also work)</li>
                  </ul>
                </div>

                <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <h3 className="font-medium text-green-900 dark:text-green-100 mb-2">
                    ✅ Current Selection:
                  </h3>
                  <div className="text-sm text-green-800 dark:text-green-200 space-y-1">
                    <p><strong>Category:</strong> {selectedCategory ? categories.find(c => c.value === selectedCategory)?.label : 'None selected'}</p>
                    <p><strong>Price Range:</strong> {selectedPrice ? priceRanges.find(p => p.value === selectedPrice)?.label : 'None selected'}</p>
                  </div>
                </div>

                <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                  <h3 className="font-medium text-yellow-900 dark:text-yellow-100 mb-2">
                    🔧 Debug Info:
                  </h3>
                  <div className="text-sm text-yellow-800 dark:text-yellow-200 space-y-1">
                    <p>• Radio inputs are invisible but clickable (opacity: 0, size: 20px)</p>
                    <p>• Z-index ensures inputs are on top of custom styling</p>
                    <p>• Labels should trigger radio button clicks</p>
                    <p>• Blue dots should appear when selected</p>
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
