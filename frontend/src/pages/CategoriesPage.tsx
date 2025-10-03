import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Container, Card, CardContent } from '@/components';
import { categories } from '@/data/products';

export const CategoriesPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container className="py-12">
      {/* Header */}
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
          Shop by Category
        </h1>
        <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
          Browse our wide selection of products across different categories
        </p>
      </div>

      {/* Categories Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-8">
        {categories.map((category) => (
          <motion.div
            key={category.slug}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
          >
            <Card
              className="group cursor-pointer hover-elevate overflow-hidden dark:bg-secondary-800 dark:border-secondary-700"
              onClick={() => navigate(`/products?category=${category.slug}`)}
            >
              <CardContent className="p-0">
                <div className="aspect-[16/9] relative overflow-hidden">
                  <img
                    src={category.image}
                    alt={category.name}
                    className="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-500"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-6">
                    <h3 className="text-2xl font-bold text-white mb-2">
                      {category.name}
                    </h3>
                    <p className="text-secondary-200 mb-4">
                      {category.subcategories.reduce((acc, sub) => acc + sub.count, 0)} Products
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {category.subcategories.map((sub) => (
                        <span
                          key={sub.slug}
                          className="text-sm bg-white/20 text-white px-3 py-1 rounded-full backdrop-blur-sm"
                        >
                          {sub.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>
    </Container>
  );
};

export default CategoriesPage;
