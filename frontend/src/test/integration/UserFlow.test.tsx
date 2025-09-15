import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { App } from '@/App'
import { useAuth } from '@/hooks/useAuth'

// Mock the useAuth hook
vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn()
}))

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

describe('User Flow Integration Tests', () => {
  const mockUseAuth = useAuth as any

  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
      isLoading: false,
      error: null
    })
  })

  it('allows user to navigate through product browsing flow', async () => {
    renderWithProviders(<App />)
    
    // Navigate to products page
    const productsLink = screen.getByRole('link', { name: /products/i })
    fireEvent.click(productsLink)
    
    // Wait for products to load
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
    })
    
    // Click on a product to view details
    const productCard = screen.getByText('Test Product')
    fireEvent.click(productCard)
    
    // Should navigate to product detail page
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
      expect(screen.getByText('Test product description')).toBeInTheDocument()
    })
  })

  it('allows user to add product to cart', async () => {
    renderWithProviders(<App />)
    
    // Navigate to products page
    const productsLink = screen.getByRole('link', { name: /products/i })
    fireEvent.click(productsLink)
    
    // Wait for products to load
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
    })
    
    // Click add to cart button
    const addToCartButton = screen.getByRole('button', { name: /add to cart/i })
    fireEvent.click(addToCartButton)
    
    // Should show success message
    await waitFor(() => {
      expect(screen.getByText(/added to cart/i)).toBeInTheDocument()
    })
  })

  it('allows user to complete checkout flow', async () => {
    // Mock authenticated user
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe'
      },
      login: vi.fn(),
      logout: vi.fn(),
      isLoading: false,
      error: null
    })
    
    renderWithProviders(<App />)
    
    // Navigate to cart
    const cartLink = screen.getByRole('link', { name: /cart/i })
    fireEvent.click(cartLink)
    
    // Wait for cart to load
    await waitFor(() => {
      expect(screen.getByText(/your cart/i)).toBeInTheDocument()
    })
    
    // Click checkout button
    const checkoutButton = screen.getByRole('button', { name: /checkout/i })
    fireEvent.click(checkoutButton)
    
    // Should navigate to checkout page
    await waitFor(() => {
      expect(screen.getByText(/checkout/i)).toBeInTheDocument()
    })
    
    // Fill in shipping information
    const firstNameInput = screen.getByLabelText(/first name/i)
    const lastNameInput = screen.getByLabelText(/last name/i)
    const addressInput = screen.getByLabelText(/address/i)
    const cityInput = screen.getByLabelText(/city/i)
    const zipCodeInput = screen.getByLabelText(/zip code/i)
    
    fireEvent.change(firstNameInput, { target: { value: 'John' } })
    fireEvent.change(lastNameInput, { target: { value: 'Doe' } })
    fireEvent.change(addressInput, { target: { value: '123 Main St' } })
    fireEvent.change(cityInput, { target: { value: 'New York' } })
    fireEvent.change(zipCodeInput, { target: { value: '10001' } })
    
    // Click place order button
    const placeOrderButton = screen.getByRole('button', { name: /place order/i })
    fireEvent.click(placeOrderButton)
    
    // Should show order confirmation
    await waitFor(() => {
      expect(screen.getByText(/order confirmed/i)).toBeInTheDocument()
    })
  })

  it('allows user to search for products', async () => {
    renderWithProviders(<App />)
    
    // Find search input
    const searchInput = screen.getByPlaceholderText(/search products/i)
    fireEvent.change(searchInput, { target: { value: 'test' } })
    
    // Press enter or click search button
    const searchButton = screen.getByRole('button', { name: /search/i })
    fireEvent.click(searchButton)
    
    // Should show search results
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
    })
  })

  it('allows user to filter products by category', async () => {
    renderWithProviders(<App />)
    
    // Navigate to products page
    const productsLink = screen.getByRole('link', { name: /products/i })
    fireEvent.click(productsLink)
    
    // Wait for products to load
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
    })
    
    // Click on category filter
    const categoryFilter = screen.getByRole('button', { name: /electronics/i })
    fireEvent.click(categoryFilter)
    
    // Should filter products
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument()
    })
  })

  it('allows user to view order history', async () => {
    // Mock authenticated user
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe'
      },
      login: vi.fn(),
      logout: vi.fn(),
      isLoading: false,
      error: null
    })
    
    renderWithProviders(<App />)
    
    // Navigate to user profile
    const profileLink = screen.getByRole('link', { name: /profile/i })
    fireEvent.click(profileLink)
    
    // Click on order history tab
    const orderHistoryTab = screen.getByRole('tab', { name: /order history/i })
    fireEvent.click(orderHistoryTab)
    
    // Should show order history
    await waitFor(() => {
      expect(screen.getByText(/order history/i)).toBeInTheDocument()
    })
  })
})
