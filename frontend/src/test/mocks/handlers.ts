import { http, HttpResponse } from 'msw'

export const handlers = [
  // Auth endpoints
  http.post('/api/auth/login', () => {
    return HttpResponse.json({
      success: true,
      data: {
        token: 'mock-jwt-token',
        user: {
          id: '1',
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe',
          roles: ['USER']
        }
      }
    })
  }),

  http.post('/api/auth/register', () => {
    return HttpResponse.json({
      success: true,
      message: 'User registered successfully'
    })
  }),

  http.post('/api/auth/logout', () => {
    return HttpResponse.json({
      success: true,
      message: 'Logged out successfully'
    })
  }),

  // Product endpoints
  http.get('/api/products', () => {
    return HttpResponse.json({
      success: true,
      data: {
        content: [
          {
            id: '1',
            name: 'Test Product',
            description: 'A test product',
            price: 99.99,
            stock: 10,
            category: 'Electronics',
            images: ['https://example.com/image.jpg']
          }
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0
      }
    })
  }),

  http.get('/api/products/:id', () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: '1',
        name: 'Test Product',
        description: 'A test product',
        price: 99.99,
        stock: 10,
        category: 'Electronics',
        images: ['https://example.com/image.jpg']
      }
    })
  }),

  // Category endpoints
  http.get('/api/categories', () => {
    return HttpResponse.json({
      success: true,
      data: [
        {
          id: '1',
          name: 'Electronics',
          description: 'Electronic products',
          isActive: true
        }
      ]
    })
  }),

  // Cart endpoints
  http.get('/api/cart', () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: '1',
        items: [],
        totalAmount: 0
      }
    })
  }),

  http.post('/api/cart/add', () => {
    return HttpResponse.json({
      success: true,
      message: 'Item added to cart'
    })
  }),

  // User endpoints
  http.get('/api/users/profile', () => {
    return HttpResponse.json({
      success: true,
      data: {
        id: '1',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        roles: ['USER']
      }
    })
  })
]