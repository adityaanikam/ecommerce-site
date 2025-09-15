import { lazy } from 'react';
import { LazyWrapper } from '@/components/LazyWrapper';

// Lazy load all pages for code splitting
export const HomePage = lazy(() => import('./HomePage').then(module => ({ default: module.HomePage })));
export const ProductsPage = lazy(() => import('./ProductsPage').then(module => ({ default: module.ProductsPage })));
export const ProductDetailPage = lazy(() => import('./ProductDetailPage').then(module => ({ default: module.ProductDetailPage })));
export const CartPage = lazy(() => import('./CartPage').then(module => ({ default: module.CartPage })));
export const CheckoutPage = lazy(() => import('./CheckoutPage').then(module => ({ default: module.CheckoutPage })));
export const AuthPage = lazy(() => import('./AuthPage').then(module => ({ default: module.AuthPage })));
export const ProfilePage = lazy(() => import('./ProfilePage').then(module => ({ default: module.ProfilePage })));
export const OrdersPage = lazy(() => import('./OrdersPage').then(module => ({ default: module.OrdersPage })));
export const OrderDetailPage = lazy(() => import('./OrderDetailPage').then(module => ({ default: module.OrderDetailPage })));
export const NotFoundPage = lazy(() => import('./NotFoundPage').then(module => ({ default: module.NotFoundPage })));

// Admin pages (lazy loaded)
export const AdminDashboard = lazy(() => import('./admin/AdminDashboard').then(module => ({ default: module.AdminDashboard })));
export const AdminProducts = lazy(() => import('./admin/AdminProducts').then(module => ({ default: module.AdminProducts })));
export const AdminOrders = lazy(() => import('./admin/AdminOrders').then(module => ({ default: module.AdminOrders })));
export const AdminUsers = lazy(() => import('./admin/AdminUsers').then(module => ({ default: module.AdminUsers })));
export const AdminAnalytics = lazy(() => import('./admin/AdminAnalytics').then(module => ({ default: module.AdminAnalytics })));

// Wrapped components with loading fallback
export const LazyHomePage = () => (
  <LazyWrapper>
    <HomePage />
  </LazyWrapper>
);

export const LazyProductsPage = () => (
  <LazyWrapper>
    <ProductsPage />
  </LazyWrapper>
);

export const LazyProductDetailPage = () => (
  <LazyWrapper>
    <ProductDetailPage />
  </LazyWrapper>
);

export const LazyCartPage = () => (
  <LazyWrapper>
    <CartPage />
  </LazyWrapper>
);

export const LazyCheckoutPage = () => (
  <LazyWrapper>
    <CheckoutPage />
  </LazyWrapper>
);

export const LazyAuthPage = () => (
  <LazyWrapper>
    <AuthPage />
  </LazyWrapper>
);

export const LazyProfilePage = () => (
  <LazyWrapper>
    <ProfilePage />
  </LazyWrapper>
);

export const LazyOrdersPage = () => (
  <LazyWrapper>
    <OrdersPage />
  </LazyWrapper>
);

export const LazyOrderDetailPage = () => (
  <LazyWrapper>
    <OrderDetailPage />
  </LazyWrapper>
);

export const LazyNotFoundPage = () => (
  <LazyWrapper>
    <NotFoundPage />
  </LazyWrapper>
);

// Admin wrapped components
export const LazyAdminDashboard = () => (
  <LazyWrapper>
    <AdminDashboard />
  </LazyWrapper>
);

export const LazyAdminProducts = () => (
  <LazyWrapper>
    <AdminProducts />
  </LazyWrapper>
);

export const LazyAdminOrders = () => (
  <LazyWrapper>
    <AdminOrders />
  </LazyWrapper>
);

export const LazyAdminUsers = () => (
  <LazyWrapper>
    <AdminUsers />
  </LazyWrapper>
);

export const LazyAdminAnalytics = () => (
  <LazyWrapper>
    <AdminAnalytics />
  </LazyWrapper>
);
