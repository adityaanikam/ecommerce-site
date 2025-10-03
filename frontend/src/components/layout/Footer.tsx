import React from 'react';
import { Link } from 'react-router-dom';
import { Container } from '@/components';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-white dark:bg-secondary-900 border-t border-secondary-200 dark:border-secondary-800">
      <Container>
        <div className="py-12">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {/* Company Info */}
            <div>
              <h3 className="text-lg font-semibold text-secondary-900 dark:text-white mb-4">
                E-Commerce Store
              </h3>
              <p className="text-secondary-600 dark:text-secondary-400 text-sm">
                Your one-stop shop for all your needs. Quality products, great prices, and excellent service.
              </p>
            </div>

            {/* Quick Links */}
            <div>
              <h3 className="text-sm font-semibold text-secondary-900 dark:text-white uppercase tracking-wider mb-4">
                Quick Links
              </h3>
              <ul className="space-y-3">
                <li>
                  <Link
                    to="/products"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    All Products
                  </Link>
                </li>
                <li>
                  <Link
                    to="/cart"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Shopping Cart
                  </Link>
                </li>
                <li>
                  <Link
                    to="/wishlist"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Wishlist
                  </Link>
                </li>
              </ul>
            </div>

            {/* Categories */}
            <div>
              <h3 className="text-sm font-semibold text-secondary-900 dark:text-white uppercase tracking-wider mb-4">
                Categories
              </h3>
              <ul className="space-y-3">
                <li>
                  <Link
                    to="/products?category=electronics"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Electronics
                  </Link>
                </li>
                <li>
                  <Link
                    to="/products?category=fashion"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Fashion
                  </Link>
                </li>
                <li>
                  <Link
                    to="/products?category=home-garden"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Home & Garden
                  </Link>
                </li>
                <li>
                  <Link
                    to="/products?category=sports"
                    className="text-secondary-600 dark:text-secondary-400 hover:text-primary-600 dark:hover:text-primary-400 text-sm"
                  >
                    Sports
                  </Link>
                </li>
              </ul>
            </div>

            {/* Contact */}
            <div>
              <h3 className="text-sm font-semibold text-secondary-900 dark:text-white uppercase tracking-wider mb-4">
                Contact Us
              </h3>
              <ul className="space-y-3 text-sm text-secondary-600 dark:text-secondary-400">
                <li>Email: support@ecommerce.com</li>
                <li>Phone: (555) 123-4567</li>
                <li>Hours: Mon-Fri 9am-5pm EST</li>
              </ul>
            </div>
          </div>

          {/* Bottom Bar */}
          <div className="mt-12 pt-8 border-t border-secondary-200 dark:border-secondary-800">
            <p className="text-center text-sm text-secondary-600 dark:text-secondary-400">
              © {new Date().getFullYear()} E-Commerce Store. All rights reserved.
            </p>
          </div>
        </div>
      </Container>
    </footer>
  );
};