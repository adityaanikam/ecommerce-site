import React from 'react';

export const RadioButtonTest: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('fashion');

  const categories = [
    { value: 'electronics', label: 'Electronics', count: 50 },
    { value: 'fashion', label: 'Fashion', count: 30 },
    { value: 'sports', label: 'Sports', count: 24 },
    { value: 'home-garden', label: 'Home & Garden', count: 24 },
  ];

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-md mx-auto">
        <h1 className="text-2xl font-bold mb-8 text-gray-900 dark:text-white">
          Radio Button Test
        </h1>
        
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
          <h2 className="text-lg font-semibold mb-6 text-gray-900 dark:text-white">
            Categories
          </h2>
          
          <div className="space-y-4">
            {categories.map((category) => (
              <div key={category.value} className="flex items-center justify-between">
                <div className="custom-radio">
                  <input
                    type="radio"
                    id={`category-${category.value}`}
                    name="category"
                    value={category.value}
                    checked={selectedCategory === category.value}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                  />
                  <label htmlFor={`category-${category.value}`} className="custom-radio-label">
                    {category.label}
                  </label>
                </div>
                <span className="text-xs bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-2 py-1 rounded-full">
                  {category.count}
                </span>
              </div>
            ))}
          </div>
          
          <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <p className="text-sm text-blue-800 dark:text-blue-200">
              <strong>Selected:</strong> {categories.find(c => c.value === selectedCategory)?.label}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
