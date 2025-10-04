# E-commerce Application

A modern, full-stack e-commerce application built with React, TypeScript, Spring Boot, and MongoDB.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ and npm
- Java 17+
- MongoDB (local or Atlas)
- Maven 3.6+

### Local Development

1. **Clone and setup**
   ```bash
   git clone <repository-url>
   cd ecommerce-project
   ```

2. **Start MongoDB** (if using local)
   ```bash
   mongod --dbpath ./data/db
   ```

3. **Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. **Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Verify Setup**
   - Backend: `http://localhost:8080/api/products/verify`
   - Frontend: `http://localhost:3000`

## 🏗️ Production Deployment

### Architecture
- **Frontend**: Vercel (React + TypeScript)
- **Backend**: Render (Dockerized Spring Boot)
- **Database**: MongoDB Atlas (Cloud)
- **Images**: Cloud Storage or Render Static Files

### Quick Deploy Steps

1. **MongoDB Atlas Setup**
   - Create cluster at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
   - Get connection string
   - Configure network access

2. **Backend to Render**
   - Connect GitHub repo to Render
   - Set environment variables:
     ```
     MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/ecommerce
     ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
     ```

3. **Frontend to Vercel**
   - Connect GitHub repo to Vercel
   - Set environment variables:
     ```
     VITE_API_BASE_URL=https://your-render-app.onrender.com/api
     VITE_IMAGE_BASE_URL=https://your-render-app.onrender.com
     ```

4. **Database Seeding**
```bash
   cd scripts
   npm install
   MONGODB_URI=your-atlas-uri npm run upload
   ```

📖 **Detailed instructions**: See [PRODUCTION_SETUP.md](./PRODUCTION_SETUP.md)

## 🎯 Features

- **Product Catalog**: Browse products by category with advanced filtering
- **Shopping Cart**: Add/remove items with quantity management
- **User Authentication**: Secure login and registration
- **Order Management**: Complete checkout process
- **Admin Dashboard**: Manage products, orders, and users
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Dark Mode**: Toggle between light and dark themes
- **Image Gallery**: Multiple images per product with zoom
- **Search & Filter**: Advanced product search and filtering

## 🛠️ Tech Stack

### Frontend
- **React 18** with TypeScript
- **Vite** for build tooling
- **Tailwind CSS** for styling
- **React Query** for state management
- **React Router** for navigation
- **Framer Motion** for animations

### Backend
- **Spring Boot 3.2** with Java 17
- **Spring Data MongoDB** for database
- **Spring Security** for authentication
- **Maven** for dependency management
- **Docker** for containerization

### Database & Storage
- **MongoDB Atlas** for cloud database
- **Cloud Storage** for product images
- **Static Files** for local development

## 📁 Project Structure

```
ecommerce-project/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration files
│   ├── Products/           # Product images
│   └── Dockerfile          # Docker configuration
├── frontend/               # React frontend
│   ├── src/               # Source code
│   ├── public/            # Static assets
│   ├── vercel.json        # Vercel configuration
│   └── dist/              # Build output
├── scripts/               # Utility scripts
│   ├── upload-to-mongodb-atlas.js
│   └── package.json
├── docker-compose.prod.yml # Production Docker
├── PRODUCTION_SETUP.md    # Deployment guide
└── ecommerce.products.json # Sample data
```

## 🔧 Configuration

### Environment Variables

#### Backend (Render)
```env
SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/ecommerce
SPRING_DATA_MONGODB_DATABASE=ecommerce
ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
PORT=8080
```

#### Frontend (Vercel)
```env
VITE_API_BASE_URL=https://your-render-app.onrender.com/api
VITE_IMAGE_BASE_URL=https://your-render-app.onrender.com
VITE_APP_NAME=E-commerce Store
VITE_APP_DESCRIPTION=Modern e-commerce platform
```

## 🚀 API Endpoints

### Products
- `GET /api/products` - Get all products with pagination
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get products by category
- `GET /api/products/featured` - Get featured products
- `GET /api/products/verify` - System verification

### Health
- `GET /api/health` - Health check endpoint

## 🐳 Docker Support

### Development
```bash
# Backend
cd backend
docker build -t ecommerce-backend .
docker run -p 8080:8080 ecommerce-backend

# Frontend
cd frontend
docker build -t ecommerce-frontend .
docker run -p 3000:3000 ecommerce-frontend
```

### Production
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 📊 Database Schema

### Products Collection
```json
{
  "_id": "ELEC001",
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "price": 999.99,
  "discountPrice": 590.75,
  "category": "Electronics",
  "subcategory": "Mobiles",
  "images": ["/products/Electronics/iPhone+15+Pro/1.jpg"],
  "description": "Latest iPhone with titanium design",
  "stock": 50,
  "rating": 4.8,
  "reviewCount": 0,
  "specs": {"Processor": "A17 Pro"},
  "features": ["5G", "Face ID"],
  "slug": "elec001",
  "isNew": false,
  "isFeatured": false,
  "isOnSale": true,
  "createdAt": 1754763566245,
  "updatedAt": 1754763566245
}
```

## 🔍 Troubleshooting

### Common Issues

1. **CORS Errors**
   - Check `ALLOWED_ORIGINS` in Render
   - Verify frontend URL is included

2. **Database Connection**
   - Verify MongoDB Atlas connection string
   - Check network access settings

3. **Image Loading**
   - Check image URLs and CORS settings
   - Verify image files exist

4. **Build Failures**
   - Check environment variables
   - Verify dependencies

### Debug Steps

1. **Backend Logs**: Check Render dashboard
2. **Frontend Logs**: Check Vercel dashboard
3. **Database**: Check MongoDB Atlas logs
4. **API Testing**: Use Postman or curl

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- React team for the amazing framework
- Spring Boot team for the robust backend
- MongoDB for the flexible database
- Vercel and Render for hosting platforms
- Tailwind CSS for the utility-first CSS framework