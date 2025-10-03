import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Footer } from './Footer';
import { CartDrawer } from '@/components/ecommerce';

export const Layout: React.FC = () => {
  const [isCartOpen, setIsCartOpen] = React.useState(false);

  return (
    <div className="min-h-screen flex flex-col">
      <Header onCartClick={() => setIsCartOpen(true)} />
      <main className="flex-grow">
        <Outlet />
      </main>
      <Footer />
      <CartDrawer open={isCartOpen} onClose={() => setIsCartOpen(false)} />
    </div>
  );
};