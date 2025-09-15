import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { LoginForm } from '@/components/auth/LoginForm'
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

describe('LoginForm', () => {
  const mockLogin = vi.fn()
  const mockUseAuth = useAuth as any

  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isLoading: false,
      error: null
    })
  })

  it('renders login form correctly', () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('shows validation errors for empty fields', async () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const submitButton = screen.getByRole('button', { name: /sign in/i })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeInTheDocument()
      expect(screen.getByText(/password is required/i)).toBeInTheDocument()
    })
  })

  it('shows validation error for invalid email', async () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const emailInput = screen.getByLabelText(/email/i)
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } })
    
    const submitButton = screen.getByRole('button', { name: /sign in/i })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(screen.getByText(/invalid email/i)).toBeInTheDocument()
    })
  })

  it('calls login function with correct credentials', async () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const emailInput = screen.getByLabelText(/email/i)
    const passwordInput = screen.getByLabelText(/password/i)
    const submitButton = screen.getByRole('button', { name: /sign in/i })
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } })
    fireEvent.change(passwordInput, { target: { value: 'password123' } })
    fireEvent.click(submitButton)
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      })
    })
  })

  it('shows loading state during login', () => {
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isLoading: true,
      error: null
    })
    
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const submitButton = screen.getByRole('button', { name: /sign in/i })
    expect(submitButton).toBeDisabled()
    expect(screen.getByText(/signing in/i)).toBeInTheDocument()
  })

  it('displays error message when login fails', () => {
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      isLoading: false,
      error: 'Invalid credentials'
    })
    
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
  })

  it('toggles password visibility', () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const passwordInput = screen.getByLabelText(/password/i)
    const toggleButton = screen.getByRole('button', { name: /toggle password visibility/i })
    
    expect(passwordInput).toHaveAttribute('type', 'password')
    
    fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'text')
    
    fireEvent.click(toggleButton)
    expect(passwordInput).toHaveAttribute('type', 'password')
  })

  it('shows remember me checkbox', () => {
    const queryClient = createTestQueryClient()
    
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/login']}>
          <LoginForm />
        </MemoryRouter>
      </QueryClientProvider>
    )
    
    const rememberMeCheckbox = screen.getByLabelText(/remember me/i)
    expect(rememberMeCheckbox).toBeInTheDocument()
  })
})
