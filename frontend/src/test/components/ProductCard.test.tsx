import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { ProductCard } from '@/components/ecommerce/ProductCard'
import { Product } from '@/types/product'

const mockProduct: Product = {
  id: '1',
  name: 'Test Product',
  description: 'Test product description',
  price: 99.99,
  discountPrice: 79.99,
  images: ['image1.jpg', 'image2.jpg'],
  category: 'Electronics',
  subcategory: 'Smartphones',
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

describe('ProductCard', () => {
  it('renders product information correctly', () => {
    render(<ProductCard product={mockProduct} />)
    
    expect(screen.getByText('Test Product')).toBeInTheDocument()
    expect(screen.getByText('Test product description')).toBeInTheDocument()
    expect(screen.getByText('$99.99')).toBeInTheDocument()
    expect(screen.getByText('$79.99')).toBeInTheDocument()
    expect(screen.getByText('TestBrand')).toBeInTheDocument()
  })

  it('displays discount badge when discount price is available', () => {
    render(<ProductCard product={mockProduct} />)
    
    const discountBadge = screen.getByText(/20% off/i)
    expect(discountBadge).toBeInTheDocument()
  })

  it('shows out of stock message when stock is 0', () => {
    const outOfStockProduct = { ...mockProduct, stock: 0 }
    render(<ProductCard product={outOfStockProduct} />)
    
    expect(screen.getByText('Out of Stock')).toBeInTheDocument()
  })

  it('displays rating stars correctly', () => {
    render(<ProductCard product={mockProduct} />)
    
    const ratingElement = screen.getByText('4.5')
    expect(ratingElement).toBeInTheDocument()
    
    const reviewCount = screen.getByText('(100)')
    expect(reviewCount).toBeInTheDocument()
  })

  it('calls onAddToCart when add to cart button is clicked', () => {
    const mockOnAddToCart = vi.fn()
    render(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />)
    
    const addToCartButton = screen.getByRole('button', { name: /add to cart/i })
    addToCartButton.click()
    
    expect(mockOnAddToCart).toHaveBeenCalledWith(mockProduct.id)
  })

  it('calls onViewDetails when view details button is clicked', () => {
    const mockOnViewDetails = vi.fn()
    render(<ProductCard product={mockProduct} onViewDetails={mockOnViewDetails} />)
    
    const viewDetailsButton = screen.getByRole('button', { name: /view details/i })
    viewDetailsButton.click()
    
    expect(mockOnViewDetails).toHaveBeenCalledWith(mockProduct.id)
  })

  it('displays product image with correct alt text', () => {
    render(<ProductCard product={mockProduct} />)
    
    const productImage = screen.getByAltText('Test Product')
    expect(productImage).toBeInTheDocument()
    expect(productImage).toHaveAttribute('src', 'image1.jpg')
  })

  it('shows loading state when product is being added to cart', () => {
    render(<ProductCard product={mockProduct} isLoading={true} />)
    
    const addToCartButton = screen.getByRole('button', { name: /add to cart/i })
    expect(addToCartButton).toBeDisabled()
  })
})
