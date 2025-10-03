import React from 'react';
import { motion } from 'framer-motion';
import { Check, Info } from 'lucide-react';
import { Card, CardContent } from '@/components';
import { cn } from '@/utils/cn';

interface ProductSpecsProps {
  specs: Record<string, string>;
  features?: string[];
  className?: string;
}

export const ProductSpecs: React.FC<ProductSpecsProps> = ({
  specs,
  features,
  className,
}) => {
  return (
    <div className={cn('space-y-6', className)}>
      {/* Specifications */}
      <Card>
        <CardContent className="p-6">
          <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100 mb-4">
            Specifications
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {Object.entries(specs).map(([key, value], index) => (
              <motion.div
                key={key}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="flex items-start space-x-3"
              >
                <div className="flex-shrink-0 w-8 h-8 rounded-full bg-primary-50 dark:bg-primary-900/30 flex items-center justify-center">
                  <Info className="h-4 w-4 text-primary-600 dark:text-primary-400" />
                </div>
                <div>
                  <h4 className="text-sm font-medium text-secondary-900 dark:text-secondary-100">
                    {key}
                  </h4>
                  <p className="text-sm text-secondary-600 dark:text-secondary-400 mt-1">
                    {value}
                  </p>
                </div>
              </motion.div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Features */}
      {features && features.length > 0 && (
        <Card>
          <CardContent className="p-6">
            <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100 mb-4">
              Key Features
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {features.map((feature, index) => (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.1 }}
                  className="flex items-start space-x-3"
                >
                  <div className="flex-shrink-0 w-5 h-5 rounded-full bg-success-50 dark:bg-success-900/30 flex items-center justify-center">
                    <Check className="h-3 w-3 text-success-600 dark:text-success-400" />
                  </div>
                  <p className="text-sm text-secondary-700 dark:text-secondary-300">
                    {feature}
                  </p>
                </motion.div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Additional Information */}
      <Card>
        <CardContent className="p-6">
          <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100 mb-4">
            Additional Information
          </h3>
          <div className="prose prose-sm dark:prose-invert max-w-none">
            <ul className="space-y-2 text-secondary-700 dark:text-secondary-300">
              <li>Free shipping on orders over $50</li>
              <li>30-day money-back guarantee</li>
              <li>24/7 customer support</li>
              <li>Secure checkout with SSL encryption</li>
            </ul>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
