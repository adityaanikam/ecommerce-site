# E-commerce Backend Application

A complete Spring Boot e-commerce backend application with MongoDB, Redis, and Docker support.

## Features

- **User Management**: Registration, authentication, and profile management
- **Product Management**: CRUD operations for products with search and filtering
- **Category Management**: Hierarchical category structure
- **Shopping Cart**: Add, update, and remove items from cart
- **Order Management**: Complete order lifecycle management
- **Security**: JWT-based authentication with role-based access control
- **Caching**: Redis integration for session management and caching
- **Database**: MongoDB for data persistence
- **Containerization**: Docker support with docker-compose

## Technology Stack

- **Java 17**
- **Spring Boot 3.2+**
- **Spring Security** with JWT
- **Spring Data MongoDB**
- **Spring Data Redis**
- **MongoDB** (Primary Database)
- **Redis** (Caching & Sessions)
- **Maven** (Build Tool)
- **Docker** (Containerization)
- **Lombok** (Code Generation)

## Project Structure

```
src/main/java/com/ecommerce/
├── controller/          # REST Controllers
├── service/            # Business Logic Layer
├── repository/         # Data Access Layer
├── model/             # Entity Models
├── dto/               # Data Transfer Objects
├── security/          # Security Configuration
├── exception/         # Exception Handling
└── config/            # Configuration Classes
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- MongoDB (if running locally)
- Redis (if running locally)

### Running with Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd ecommerce-backend
```

2. Start all services:
```bash
docker-compose up -d
```

This will start:
- MongoDB on port 27017
- Redis on port 6379
- Spring Boot application on port 8080

3. Access the application:
- API Base URL: `http://localhost:8080/api`
- Health Check: `http://localhost:8080/api/actuator/health`

### Running Locally

1. Start MongoDB and Redis:
```bash
# MongoDB
mongod

# Redis
redis-server
```

2. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Products
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?q={query}` - Search products
- `GET /api/products/category/{category}` - Get products by category
- `POST /api/products` - Create product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)
- `DELETE /api/products/{id}` - Delete product (Admin only)

### Cart
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items?productId={id}&quantity={qty}` - Add item to cart
- `PUT /api/cart/items/{productId}?quantity={qty}` - Update cart item
- `DELETE /api/cart/items/{productId}` - Remove item from cart
- `DELETE /api/cart` - Clear cart

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/admin/all` - Get all orders (Admin only)
- `PUT /api/orders/{id}/status` - Update order status (Admin only)

### Users
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

## Configuration

### Environment Variables

- `JWT_SECRET`: Secret key for JWT token generation
- `GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 client secret
- `SPRING_DATA_MONGODB_URI`: MongoDB connection string
- `SPRING_DATA_REDIS_HOST`: Redis host
- `SPRING_DATA_REDIS_PASSWORD`: Redis password

### Application Profiles

- `default`: Local development configuration
- `docker`: Docker container configuration

## Database Schema

### Users Collection
- User authentication and profile information
- Role-based access control (USER, ADMIN, MODERATOR)

### Products Collection
- Product information with specifications
- Text search capabilities
- Category and brand associations

### Categories Collection
- Hierarchical category structure
- Parent-child relationships

### Carts Collection
- User shopping cart with items
- Automatic total calculations

### Orders Collection
- Complete order information
- Order status tracking
- Payment and shipping details

## Security

- JWT-based authentication
- Role-based authorization
- Password encryption with BCrypt
- CORS configuration
- Input validation

## Caching

- Redis integration for session management
- Product and user data caching
- Configurable cache TTL

## Monitoring

- Spring Boot Actuator endpoints
- Health checks
- Metrics collection

## Development

### Building the Application
```bash
mvn clean package
```

### Running Tests
```bash
mvn test
```

### Code Quality
The project uses:
- Lombok for reducing boilerplate code
- Validation annotations for input validation
- Proper exception handling
- Logging with SLF4J

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
