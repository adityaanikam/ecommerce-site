import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ShoppingCart, Check } from 'lucide-react';

interface AddToCartAnimationProps {
  isAnimating: boolean;
  onAnimationComplete: () => void;
}

export const AddToCartAnimation: React.FC<AddToCartAnimationProps> = ({
  isAnimating,
  onAnimationComplete,
}) => {
  return (
    <AnimatePresence>
      {isAnimating && (
        <motion.div
          initial={{ opacity: 0, scale: 0.5 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.5 }}
          className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50"
          onAnimationComplete={onAnimationComplete}
        >
          <div className="bg-primary-600 text-white rounded-full p-4 shadow-lg">
            <motion.div
              initial={{ scale: 1 }}
              animate={{
                scale: [1, 1.2, 1],
                rotate: [0, 10, -10, 0],
              }}
              transition={{ duration: 0.5 }}
            >
              <Check className="h-8 w-8" />
            </motion.div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};
