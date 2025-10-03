import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Heart, Sun, Moon, Menu, X, Search } from 'lucide-react';
import { Button, Container, Input, Badge } from '@/components';
import '@/styles/header.css';
import { CartDrawer } from '@/components/ecommerce';
import { useCart } from '@/contexts/CartContext';
import { useWishlist } from '@/contexts/WishlistContext';

export const Header: React.FC = () => {
  const [isScrolled, setIsScrolled] = React.useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false);
  const [isCartOpen, setIsCartOpen] = React.useState(false);
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const { state: cartState } = useCart();
  const { state: wishlistState } = useWishlist();

  React.useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 0);
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  React.useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkMode]);

  return (
    <>
      <header
        className={`header ${isScrolled ? 'header-scrolled' : 'header-default'}`}
      >
        <Container>
          <div className="flex h-16 items-center justify-between">
            {/* Logo */}
            <Link
              to="/"
              className={`logo ${isScrolled ? 'scale-90' : 'scale-100'}`}
            >
              EcoShop
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center space-x-8">
              <Link
                to="/products"
                className="nav-link"
              >
                Products
              </Link>
              <Link
                to="/categories"
                className="nav-link"
              >
                Categories
              </Link>
              <Link
                to="/deals"
                className="nav-link"
              >
                Deals
              </Link>
            </nav>

            {/* Search Bar */}
            <div className="hidden lg:block flex-1 max-w-md mx-8">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
                <Input
                  type="search"
                  placeholder="Search products..."
                  className="search-input"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center space-x-4">
              {/* Theme Toggle */}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setIsDarkMode(!isDarkMode)}
                className="action-button"
              >
                {isDarkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </Button>

              {/* Wishlist */}
              <Link to="/wishlist">
                <Button
                  variant="ghost"
                  size="icon"
                  className="action-button relative"
                >
                  <Heart className="h-5 w-5" />
                  <Badge
                    variant="primary"
                    className="absolute -top-1 -right-1 h-4 w-4 p-0 flex items-center justify-center text-[10px]"
                  >
                    {wishlistState.items.length}
                  </Badge>
                </Button>
              </Link>

              {/* Cart */}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setIsCartOpen(true)}
                className="action-button relative"
              >
                <ShoppingCart className="h-5 w-5" />
                {cartState.totalItems > 0 && (
                  <Badge
                    variant="primary"
                    className="absolute -top-1 -right-1 h-4 w-4 p-0 flex items-center justify-center text-[10px]"
                  >
                    {cartState.totalItems}
                  </Badge>
                )}
              </Button>

              {/* Mobile Menu Toggle */}
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="md:hidden"
              >
                {isMobileMenuOpen ? (
                  <X className="h-5 w-5" />
                ) : (
                  <Menu className="h-5 w-5" />
                )}
              </Button>
            </div>
          </div>

          {/* Mobile Search (Visible on scroll) */}
          <div
            className={`lg:hidden pb-4 transition-all duration-300 ${
              isScrolled ? 'opacity-100' : 'opacity-0 pointer-events-none'
            }`}
          >
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-secondary-400" />
              <Input
                type="search"
                placeholder="Search products..."
                className="pl-10 w-full"
              />
            </div>
          </div>
        </Container>

        {/* Mobile Menu */}
        <div
          className={`mobile-menu transition-all duration-300 ${
            isMobileMenuOpen ? 'max-h-screen' : 'max-h-0 overflow-hidden'
          }`}
        >
          <Container>
            <nav className="py-4 space-y-2">
              <Link
                to="/products"
                className="block py-2 text-secondary-600 dark:text-secondary-400 hover:text-secondary-900 dark:hover:text-secondary-100"
                onClick={() => setIsMobileMenuOpen(false)}
              >
                Products
              </Link>
              <Link
                to="/categories"
                className="block py-2 text-secondary-600 dark:text-secondary-400 hover:text-secondary-900 dark:hover:text-secondary-100"
                onClick={() => setIsMobileMenuOpen(false)}
              >
                Categories
              </Link>
              <Link
                to="/deals"
                className="block py-2 text-secondary-600 dark:text-secondary-400 hover:text-secondary-900 dark:hover:text-secondary-100"
                onClick={() => setIsMobileMenuOpen(false)}
              >
                Deals
              </Link>
            </nav>
          </Container>
        </div>
      </header>

      {/* Cart Drawer */}
      <CartDrawer isOpen={isCartOpen} onClose={() => setIsCartOpen(false)} />
    </>
  );
};