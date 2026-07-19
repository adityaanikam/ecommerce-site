import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, Instagram, Twitter, Facebook } from 'lucide-react';
import { Container } from '@/components';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-secondary-50 dark:bg-secondary-900 border-t border-secondary-200 dark:border-secondary-800">
      <Container>
        <div className="py-12">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {/* Company Info */}
            <div>
              <div className="flex items-center gap-2 mb-4">
                <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 shadow-sm">
                  <ShoppingBag className="h-4 w-4 text-white" />
                </span>
                <h3 className="text-lg font-bold bg-gradient-to-r from-primary-600 to-primary-400 dark:from-primary-400 dark:to-primary-200 bg-clip-text text-transparent">
                  EcoShop
                </h3>
              </div>
              <p className="text-secondary-600 dark:text-secondary-400 text-sm">
                Your one-stop shop for all your needs. Quality products, great prices, and excellent service.
              </p>
              <div className="flex items-center gap-3 mt-5">
                <a
                  href="#"
                  aria-label="Instagram"
                  className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary-200/60 dark:bg-secondary-800 text-secondary-600 dark:text-secondary-400 hover:bg-primary-600 hover:text-white transition-colors"
                >
                  <Instagram className="h-4 w-4" />
                </a>
                <a
                  href="#"
                  aria-label="Twitter"
                  className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary-200/60 dark:bg-secondary-800 text-secondary-600 dark:text-secondary-400 hover:bg-primary-600 hover:text-white transition-colors"
                >
                  <Twitter className="h-4 w-4" />
                </a>
                <a
                  href="#"
                  aria-label="Facebook"
                  className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary-200/60 dark:bg-secondary-800 text-secondary-600 dark:text-secondary-400 hover:bg-primary-600 hover:text-white transition-colors"
                >
                  <Facebook className="h-4 w-4" />
                </a>
              </div>
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
              © {new Date().getFullYear()} EcoShop. All rights reserved.
            </p>
          </div>
        </div>
      </Container>
    </footer>
  );
};