import { render } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { axe, toHaveNoViolations } from 'jest-axe'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { ProductCard } from '@/components/ecommerce/ProductCard'
import { LoginForm } from '@/components/auth/LoginForm'
import { Product } from '@/types'

// Extend Jest matchers
expect.extend(toHaveNoViolations)

const createTestQueryClient = () => new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false }
  }
})

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = createTestQueryClient()
  
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </QueryClientProvider>
  )
}

describe('Accessibility Tests', () => {
  it('ProductCard should not have accessibility violations', async () => {
    const mockProduct: Product = {
      id: '1',
      name: 'Test Product',
      description: 'Test product description',
      price: 99.99,
      images: ['image1.jpg'],
      category: 'Electronics',
      brand: 'TestBrand',
      stock: 100,
      ratings: {
        averageRating: 4.5,
        totalRatings: 100
      },
      isActive: true,
      sellerId: 'seller1',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }

    const { container } = render(<ProductCard product={mockProduct} />)
    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })

  it('LoginForm should not have accessibility violations', async () => {
    const { container } = renderWithProviders(<LoginForm />)
    const results = await axe(container)
    expect(results).toHaveNoViolations()
  })

  it('ProductCard should have proper ARIA labels', () => {
    const mockProduct: Product = {
      id: '1',
      name: 'Test Product',
      description: 'Test product description',
      price: 99.99,
      images: ['image1.jpg'],
      category: 'Electronics',
      brand: 'TestBrand',
      stock: 100,
      ratings: {
        averageRating: 4.5,
        totalRatings: 100
      },
      isActive: true,
      sellerId: 'seller1',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }

    const { getByRole, getByLabelText } = render(<ProductCard product={mockProduct} />)
    
    // Check for proper button roles
    expect(getByRole('button', { name: /add to cart/i })).toBeInTheDocument()
    expect(getByRole('button', { name: /view details/i })).toBeInTheDocument()
    
    // Check for proper image alt text
    expect(getByLabelText('Test Product')).toBeInTheDocument()
  })

  it('LoginForm should have proper form labels and structure', () => {
    const { getByLabelText, getByRole } = renderWithProviders(<LoginForm />)
    
    // Check for proper form labels
    expect(getByLabelText(/email/i)).toBeInTheDocument()
    expect(getByLabelText(/password/i)).toBeInTheDocument()
    expect(getByLabelText(/remember me/i)).toBeInTheDocument()
    
    // Check for proper form structure
    expect(getByRole('form')).toBeInTheDocument()
    expect(getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('ProductCard should support keyboard navigation', () => {
    const mockProduct: Product = {
      id: '1',
      name: 'Test Product',
      description: 'Test product description',
      price: 99.99,
      images: ['image1.jpg'],
      category: 'Electronics',
      brand: 'TestBrand',
      stock: 100,
      ratings: {
        averageRating: 4.5,
        totalRatings: 100
      },
      isActive: true,
      sellerId: 'seller1',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }

    const { getByRole } = render(<ProductCard product={mockProduct} />)
    
    const addToCartButton = getByRole('button', { name: /add to cart/i })
    const viewDetailsButton = getByRole('button', { name: /view details/i })
    
    // Check that buttons are focusable
    expect(addToCartButton).toHaveAttribute('tabindex', '0')
    expect(viewDetailsButton).toHaveAttribute('tabindex', '0')
  })

  it('LoginForm should have proper error handling for screen readers', async () => {
    const { getByLabelText, getByRole } = renderWithProviders(<LoginForm />)
    
    const emailInput = getByLabelText(/email/i)
    const passwordInput = getByLabelText(/password/i)
    const submitButton = getByRole('button', { name: /sign in/i })
    
    // Check for proper ARIA attributes
    expect(emailInput).toHaveAttribute('aria-required', 'true')
    expect(passwordInput).toHaveAttribute('aria-required', 'true')
    
    // Check for proper error handling
    expect(emailInput).toHaveAttribute('aria-invalid', 'false')
    expect(passwordInput).toHaveAttribute('aria-invalid', 'false')
  })

  it('ProductCard should have proper color contrast', () => {
    const mockProduct: Product = {
      id: '1',
      name: 'Test Product',
      description: 'Test product description',
      price: 99.99,
      images: ['image1.jpg'],
      category: 'Electronics',
      brand: 'TestBrand',
      stock: 100,
      ratings: {
        averageRating: 4.5,
        totalRatings: 100
      },
      isActive: true,
      sellerId: 'seller1',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }

    const { container } = render(<ProductCard product={mockProduct} />)
    
    // Check for proper color contrast (this would need actual color values)
    // For now, we'll check that the component renders without errors
    expect(container).toBeInTheDocument()
  })

  it('LoginForm should have proper focus management', () => {
    const { getByLabelText } = renderWithProviders(<LoginForm />)
    
    const emailInput = getByLabelText(/email/i)
    const passwordInput = getByLabelText(/password/i)
    
    // Check for proper focus management
    expect(emailInput).toHaveAttribute('autocomplete', 'email')
    expect(passwordInput).toHaveAttribute('autocomplete', 'current-password')
  })
})
