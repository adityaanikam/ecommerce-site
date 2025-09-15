import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, User, Search, Menu, X, Heart, Settings, LogOut, Moon, Sun } from 'lucide-react';
import { Button, Avatar, Dropdown, DropdownOption } from '@/components/ui';
import { useAuth } from '@/contexts';
import { useCart } from '@/contexts';
import { useTheme } from '@/contexts';

export const Header: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const { totalItems } = useCart();
  const { isDark, toggleTheme } = useTheme();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false);
  const [searchQuery, setSearchQuery] = React.useState('');

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      // Navigate to search results
      window.location.href = `/products?search=${encodeURIComponent(searchQuery)}`;
    }
  };

  const userMenuOptions: DropdownOption[] = [
    {
      value: 'profile',
      label: 'Profile',
      icon: <User className="h-4 w-4" />,
    },
    {
      value: 'orders',
      label: 'Orders',
      icon: <ShoppingCart className="h-4 w-4" />,
    },
    {
      value: 'wishlist',
      label: 'Wishlist',
      icon: <Heart className="h-4 w-4" />,
    },
    {
      value: 'settings',
      label: 'Settings',
      icon: <Settings className="h-4 w-4" />,
    },
  ];

  return (
    <header className="bg-white shadow-sm border-b border-secondary-200">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-lg">E</span>
            </div>
            <span className="text-xl font-bold text-secondary-900">E-commerce</span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-8">
            <Link
              to="/products"
              className="text-secondary-600 hover:text-primary-600 transition-colors"
            >
              Products
            </Link>
            <Link
              to="/categories"
              className="text-secondary-600 hover:text-primary-600 transition-colors"
            >
              Categories
            </Link>
            <Link
              to="/about"
              className="text-secondary-600 hover:text-primary-600 transition-colors"
            >
              About
            </Link>
          </nav>

          {/* Search Bar */}
          <div className="hidden md:flex items-center flex-1 max-w-md mx-8">
            <form onSubmit={handleSearch} className="relative w-full">
              <input
                type="text"
                placeholder="Search products..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-secondary-800 dark:border-secondary-600 dark:text-white"
              />
              <Search className="absolute left-3 top-2.5 h-5 w-5 text-secondary-400" />
            </form>
          </div>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            {/* Theme Toggle */}
            <Button
              variant="ghost"
              size="icon-sm"
              onClick={toggleTheme}
              className="text-secondary-600 hover:text-primary-600"
            >
              {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>

            {/* Cart */}
            <Link to="/cart" className="relative p-2 text-secondary-600 hover:text-primary-600 transition-colors">
              <ShoppingCart className="h-6 w-6" />
              {totalItems > 0 && (
                <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                  {totalItems}
                </span>
              )}
            </Link>

            {/* User Menu */}
            {isAuthenticated ? (
              <Dropdown
                options={userMenuOptions}
                value=""
                placeholder={
                  <div className="flex items-center space-x-2">
                    <Avatar
                      src={user?.imageUrl}
                      fallback={`${user?.firstName} ${user?.lastName}`}
                      size="sm"
                    />
                    <span className="hidden sm:block text-sm font-medium">
                      {user?.firstName}
                    </span>
                  </div>
                }
                onChange={(value) => {
                  switch (value) {
                    case 'profile':
                      window.location.href = '/profile';
                      break;
                    case 'orders':
                      window.location.href = '/orders';
                      break;
                    case 'wishlist':
                      window.location.href = '/wishlist';
                      break;
                    case 'settings':
                      window.location.href = '/settings';
                      break;
                  }
                }}
                className="min-w-[200px]"
              />
            ) : (
              <div className="flex items-center space-x-2">
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Login
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">
                    Sign Up
                  </Button>
                </Link>
              </div>
            )}

            {/* Mobile Menu Button */}
            <button
              className="md:hidden p-2 text-secondary-600 hover:text-primary-600 transition-colors"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden border-t border-secondary-200 py-4">
            <div className="flex flex-col space-y-4">
              {/* Mobile Search */}
              <form onSubmit={handleSearch} className="relative">
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-secondary-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-secondary-800 dark:border-secondary-600 dark:text-white"
                />
                <Search className="absolute left-3 top-2.5 h-5 w-5 text-secondary-400" />
              </form>

              {/* Mobile Navigation */}
              <nav className="flex flex-col space-y-2">
                <Link
                  to="/products"
                  className="text-secondary-600 hover:text-primary-600 transition-colors py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Products
                </Link>
                <Link
                  to="/categories"
                  className="text-secondary-600 hover:text-primary-600 transition-colors py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  Categories
                </Link>
                <Link
                  to="/about"
                  className="text-secondary-600 hover:text-primary-600 transition-colors py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  About
                </Link>
              </nav>

              {/* Mobile Auth */}
              {!isAuthenticated && (
                <div className="flex flex-col space-y-2 pt-4 border-t border-secondary-200">
                  <Link to="/login" onClick={() => setIsMobileMenuOpen(false)}>
                    <Button variant="ghost" className="w-full">
                      Login
                    </Button>
                  </Link>
                  <Link to="/register" onClick={() => setIsMobileMenuOpen(false)}>
                    <Button className="w-full">
                      Sign Up
                    </Button>
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
};
