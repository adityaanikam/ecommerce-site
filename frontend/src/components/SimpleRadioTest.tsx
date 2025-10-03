import React from 'react';

export const SimpleRadioTest: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = React.useState('electronics');
  const [selectedPrice, setSelectedPrice] = React.useState('under-50');

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
          Simple Radio Test - Blue Dots
        </h1>
        
        <div className="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-lg">
          <h2 className="text-lg font-semibold mb-6 text-gray-900 dark:text-white">
            Test Radio Buttons
          </h2>
          
          {/* Simple Categories Test */}
          <div className="mb-6">
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
              Categories (Simple Structure)
            </h4>
            <div className="space-y-3">
              <div className="custom-radio">
                <input
                  type="radio"
                  id="electronics-simple"
                  name="category-simple"
                  checked={selectedCategory === 'electronics'}
                  onChange={() => setSelectedCategory('electronics')}
                />
                <label htmlFor="electronics-simple" className="custom-radio-label">
                  Electronics
                </label>
              </div>
              
              <div className="custom-radio">
                <input
                  type="radio"
                  id="fashion-simple"
                  name="category-simple"
                  checked={selectedCategory === 'fashion'}
                  onChange={() => setSelectedCategory('fashion')}
                />
                <label htmlFor="fashion-simple" className="custom-radio-label">
                  Fashion
                </label>
              </div>
            </div>
          </div>

          {/* Simple Price Test */}
          <div className="mb-6">
            <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
              Price Range (Simple Structure)
            </h4>
            <div className="space-y-3">
              <div className="custom-radio">
                <input
                  type="radio"
                  id="under-50-simple"
                  name="price-simple"
                  checked={selectedPrice === 'under-50'}
                  onChange={() => setSelectedPrice('under-50')}
                />
                <label htmlFor="under-50-simple" className="custom-radio-label">
                  Under $50
                </label>
              </div>
              
              <div className="custom-radio">
                <input
                  type="radio"
                  id="50-100-simple"
                  name="price-simple"
                  checked={selectedPrice === '50-100'}
                  onChange={() => setSelectedPrice('50-100')}
                />
                <label htmlFor="50-100-simple" className="custom-radio-label">
                  $50 - $100
                </label>
              </div>
            </div>
          </div>

          {/* Results */}
          <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <h3 className="font-medium text-blue-900 dark:text-blue-100 mb-2">
              Current Selection:
            </h3>
            <div className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
              <p><strong>Category:</strong> {selectedCategory}</p>
              <p><strong>Price Range:</strong> {selectedPrice}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
