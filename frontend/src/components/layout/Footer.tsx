import React from 'react';
import { Link } from 'react-router-dom';
import { Facebook, Twitter, Instagram, Mail, Phone, MapPin } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-secondary-900 text-white">
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">E</span>
              </div>
              <span className="text-xl font-bold">E-commerce</span>
            </div>
            <p className="text-secondary-300 mb-4">
              Your one-stop shop for all your needs. Quality products, great prices, and excellent service.
            </p>
            <div className="flex space-x-4">
              <a href="#" className="text-secondary-400 hover:text-white transition-colors">
                <Facebook className="h-5 w-5" />
              </a>
              <a href="#" className="text-secondary-400 hover:text-white transition-colors">
                <Twitter className="h-5 w-5" />
              </a>
              <a href="#" className="text-secondary-400 hover:text-white transition-colors">
                <Instagram className="h-5 w-5" />
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/products" className="text-secondary-300 hover:text-white transition-colors">
                  Products
                </Link>
              </li>
              <li>
                <Link to="/categories" className="text-secondary-300 hover:text-white transition-colors">
                  Categories
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-secondary-300 hover:text-white transition-colors">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-secondary-300 hover:text-white transition-colors">
                  Contact
                </Link>
              </li>
            </ul>
          </div>

          {/* Customer Service */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Customer Service</h3>
            <ul className="space-y-2">
              <li>
                <Link to="/help" className="text-secondary-300 hover:text-white transition-colors">
                  Help Center
                </Link>
              </li>
              <li>
                <Link to="/shipping" className="text-secondary-300 hover:text-white transition-colors">
                  Shipping Info
                </Link>
              </li>
              <li>
                <Link to="/returns" className="text-secondary-300 hover:text-white transition-colors">
                  Returns
                </Link>
              </li>
              <li>
                <Link to="/faq" className="text-secondary-300 hover:text-white transition-colors">
                  FAQ
                </Link>
              </li>
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Contact Info</h3>
            <div className="space-y-3">
              <div className="flex items-center space-x-3">
                <Mail className="h-5 w-5 text-primary-400" />
                <span className="text-secondary-300">support@ecommerce.com</span>
              </div>
              <div className="flex items-center space-x-3">
                <Phone className="h-5 w-5 text-primary-400" />
                <span className="text-secondary-300">+1 (555) 123-4567</span>
              </div>
              <div className="flex items-start space-x-3">
                <MapPin className="h-5 w-5 text-primary-400 mt-0.5" />
                <span className="text-secondary-300">
                  123 Commerce St<br />
                  Business City, BC 12345
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="border-t border-secondary-800 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <p className="text-secondary-400 text-sm">
              © 2024 E-commerce Store. All rights reserved.
            </p>
            <div className="flex space-x-6 mt-4 md:mt-0">
              <Link to="/privacy" className="text-secondary-400 hover:text-white text-sm transition-colors">
                Privacy Policy
              </Link>
              <Link to="/terms" className="text-secondary-400 hover:text-white text-sm transition-colors">
                Terms of Service
              </Link>
              <Link to="/cookies" className="text-secondary-400 hover:text-white text-sm transition-colors">
                Cookie Policy
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};
