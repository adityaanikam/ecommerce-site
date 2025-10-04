# Product Image Serving Configuration

This document explains how to serve product images from your local `Products` folder through Spring Boot.

## Configuration Overview

### 1. WebConfig.java (Enhanced)
**Location:** `src/main/java/com/ecommerce/config/WebConfig.java`

**Key Features:**
- Maps `/products/**` URLs to serve files from the Products folder
- Handles URL decoding (+ characters to spaces)
- Sets appropriate content-type headers for different image formats
- Configures CORS for frontend requests
- Adds caching headers for better performance

### 2. Application Properties
**Location:** `src/main/resources/application.properties`

**Added Settings:**
```properties
# Static resource configuration
spring.web.resources.static-locations=classpath:/static/,classpath:/public/,file:C:\Users\adity\IdeaProjects\ecommerece project\backend\Products\
spring.web.resources.cache.cachecontrol.max-age=3600
spring.web.resources.cache.cachecontrol.cache-public=true

# File upload configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. ImageController.java (Optional)
**Location:** `src/main/java/com/ecommerce/controller/ImageController.java`

**Purpose:** Alternative API endpoint for image serving with more control over headers and error handling.

## How It Works

### URL Mapping
- **Frontend Request:** `http://localhost:8080/products/Electronics/Airpods+Pro+2/1.webp`
- **File System Path:** `C:\Users\adity\IdeaProjects\ecommerece project\backend\Products\Electronics\Airpods Pro 2\1.webp`
- **URL Decoding:** `+` characters are automatically converted to spaces

### Content Types Supported
- `.jpg`, `.jpeg` → `image/jpeg`
- `.png` → `image/png`
- `.webp` → `image/webp`
- `.gif` → `image/gif`

### CORS Configuration
- **Allowed Origins:** `http://localhost:3000`, `http://localhost:3001`
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Cache Duration:** 1 hour (3600 seconds)

## Usage Examples

### Frontend Image URLs
```javascript
// React component example
const ProductImage = ({ product }) => {
  const imageUrl = `http://localhost:8080/products/${product.category}/${product.name.replace(/\s+/g, '+')}/1.webp`;
  
  return (
    <img 
      src={imageUrl} 
      alt={product.name}
      onError={(e) => {
        e.target.src = '/placeholder-image.png'; // Fallback image
      }}
    />
  );
};
```

### API Endpoint (Alternative)
```javascript
// Using the ImageController endpoint
const imageUrl = `http://localhost:8080/api/images/product/${category}/${productName}/${imageName}`;
```

## Testing the Configuration

### 1. Start Spring Boot Application
```bash
cd backend
mvn spring-boot:run
```

### 2. Test Image URLs
Open these URLs in your browser:
- `http://localhost:8080/products/Electronics/Airpods+Pro+2/1.webp`
- `http://localhost:8080/products/Electronics/Google+Pixel+8/1.jpg`
- `http://localhost:8080/products/Electronics/Bluetooth+Speaker/1.webp`

### 3. Test from Frontend
```javascript
// Test CORS from frontend
fetch('http://localhost:8080/products/Electronics/Airpods+Pro+2/1.webp')
  .then(response => {
    if (response.ok) {
      console.log('Image loaded successfully');
    } else {
      console.log('Image not found');
    }
  })
  .catch(error => console.error('CORS or network error:', error));
```

## Troubleshooting

### Common Issues

1. **404 Not Found**
   - Check if the file exists in the Products folder
   - Verify the URL encoding (spaces should be +)
   - Ensure the path structure matches: `Products/Category/Product Name/image.ext`

2. **CORS Errors**
   - Verify the frontend URL is in the allowed origins list
   - Check if the request is coming from the correct port

3. **Wrong Content Type**
   - The interceptor automatically sets content-type based on file extension
   - Check browser developer tools for the actual content-type header

4. **File Path Issues**
   - Ensure the absolute path in WebConfig matches your actual file system
   - Use forward slashes in the file path configuration

### Debug Steps

1. **Check File Existence**
   ```bash
   # Verify the file exists
   dir "C:\Users\adity\IdeaProjects\ecommerece project\backend\Products\Electronics\Airpods Pro 2\1.webp"
   ```

2. **Test Direct File Access**
   - Try accessing the file directly through Windows Explorer
   - Check file permissions

3. **Spring Boot Logs**
   - Enable debug logging for resource handling:
   ```properties
   logging.level.org.springframework.web.servlet.resource=DEBUG
   ```

## Performance Considerations

- **Caching:** Images are cached for 1 hour to improve performance
- **Content-Type:** Proper MIME types help browsers handle images correctly
- **CORS:** Configured for development; adjust for production

## Production Considerations

For production deployment:

1. **Update CORS Origins**
   ```java
   .allowedOrigins("https://yourdomain.com")
   ```

2. **Use Environment Variables**
   ```properties
   products.image.path=${PRODUCTS_IMAGE_PATH:./Products/}
   ```

3. **CDN Integration**
   - Consider using a CDN for better performance
   - Update image URLs to point to CDN endpoints

4. **Security**
   - Add authentication if needed
   - Validate file paths to prevent directory traversal attacks
