# Environment Variables Reference

## Backend (Render) Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection string | `mongodb+srv://user:pass@cluster.mongodb.net/ecommerce` |
| `SPRING_DATA_MONGODB_DATABASE` | MongoDB database name | `ecommerce` |
| `ALLOWED_ORIGINS` | CORS allowed origins (comma-separated) | `https://your-app.vercel.app,http://localhost:3000` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `ADMIN_USERNAME` | Admin username | `admin` |
| `ADMIN_PASSWORD` | Admin password | `admin123` |

## Frontend (Vercel) Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `https://your-render-app.onrender.com/api` |
| `VITE_IMAGE_BASE_URL` | Image base URL | `https://your-render-app.onrender.com` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_APP_NAME` | Application name | `E-commerce Store` |
| `VITE_APP_DESCRIPTION` | Application description | `Modern e-commerce platform` |

## Local Development

### Backend (.env or application.properties)
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce
spring.data.mongodb.database=ecommerce
```

### Frontend (.env.local)
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_IMAGE_BASE_URL=http://localhost:8080
```

## Production Setup

### Render Environment Variables
```env
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/ecommerce?retryWrites=true&w=majority
SPRING_DATA_MONGODB_DATABASE=ecommerce
ALLOWED_ORIGINS=https://your-vercel-app.vercel.app,http://localhost:3000
PORT=8080
```

### Vercel Environment Variables
```env
VITE_API_BASE_URL=https://your-render-app.onrender.com/api
VITE_IMAGE_BASE_URL=https://your-render-app.onrender.com
VITE_APP_NAME=E-commerce Store
VITE_APP_DESCRIPTION=Modern e-commerce platform
```

## MongoDB Atlas Connection String Format

```
mongodb+srv://<username>:<password>@<cluster-url>/<database>?retryWrites=true&w=majority
```

### Example:
```
mongodb+srv://ecommerce-user:MySecurePassword123@cluster0.abc123.mongodb.net/ecommerce?retryWrites=true&w=majority
```

## Environment Variable Validation

### Backend Validation
The application will validate these environment variables on startup:
- `SPRING_DATA_MONGODB_URI` must be a valid MongoDB connection string
- `SPRING_DATA_MONGODB_DATABASE` must be a valid database name
- `ALLOWED_ORIGINS` must contain at least one valid origin

### Frontend Validation
The frontend will use fallback values if environment variables are not set:
- `VITE_API_BASE_URL` falls back to `http://localhost:8080/api`
- `VITE_IMAGE_BASE_URL` falls back to `http://localhost:8080`

## Troubleshooting

### Common Issues

1. **MongoDB Connection Failed**
   - Check `SPRING_DATA_MONGODB_URI` format
   - Verify MongoDB Atlas network access
   - Ensure database user has correct permissions

2. **CORS Errors**
   - Verify `ALLOWED_ORIGINS` includes your frontend URL
   - Check that URLs are comma-separated without spaces

3. **API Connection Failed**
   - Verify `VITE_API_BASE_URL` points to correct backend
   - Check that backend is running and accessible

4. **Image Loading Issues**
   - Verify `VITE_IMAGE_BASE_URL` points to correct backend
   - Check that images are accessible via the backend

### Debug Commands

```bash
# Test MongoDB connection
mongosh "your-connection-string"

# Test API endpoint
curl https://your-render-app.onrender.com/api/health

# Test image endpoint
curl https://your-render-app.onrender.com/products/Electronics/iPhone+15+Pro/1.jpg
```
