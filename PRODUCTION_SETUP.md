# Production Deployment Guide

## Overview
This guide covers deploying the e-commerce application to production using:
- **Backend**: Render (Dockerized Spring Boot)
- **Frontend**: Vercel (React + TypeScript)
- **Database**: MongoDB Atlas (Cloud)

## Prerequisites
1. GitHub repository with your code
2. MongoDB Atlas account
3. Render account
4. Vercel account

## Step 1: MongoDB Atlas Setup

### 1.1 Create MongoDB Atlas Cluster
1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a new cluster
3. Choose your preferred region
4. Select M0 (Free tier) for development

### 1.2 Configure Database Access
1. Go to "Database Access" in Atlas
2. Create a new database user:
   - Username: `ecommerce-user`
   - Password: Generate a secure password
   - Database User Privileges: "Read and write to any database"

### 1.3 Configure Network Access
1. Go to "Network Access" in Atlas
2. Add IP Address: `0.0.0.0/0` (Allow access from anywhere)
3. Or add specific Render IP ranges

### 1.4 Get Connection String
1. Go to "Clusters" → "Connect"
2. Choose "Connect your application"
3. Copy the connection string
4. Replace `<password>` with your database user password
5. Replace `<dbname>` with `ecommerce`

Example: `mongodb+srv://ecommerce-user:password@cluster0.xxxxx.mongodb.net/ecommerce?retryWrites=true&w=majority`

## Step 2: Render Backend Deployment

### 2.1 Create Render Service
1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click "New" → "Web Service"
3. Connect your GitHub repository
4. Select the repository

### 2.2 Configure Build Settings
- **Name**: `ecommerce-backend`
- **Root Directory**: `backend`
- **Environment**: `Docker`
- **Dockerfile Path**: `backend/Dockerfile`

### 2.3 Set Environment Variables
Add these environment variables in Render:

```
SPRING_DATA_MONGODB_URI=mongodb+srv://ecommerce-user:password@cluster0.xxxxx.mongodb.net/ecommerce?retryWrites=true&w=majority
SPRING_DATA_MONGODB_DATABASE=ecommerce
ALLOWED_ORIGINS=https://your-vercel-app.vercel.app,http://localhost:3000
PORT=8080
```

### 2.4 Deploy
1. Click "Create Web Service"
2. Render will build and deploy your backend
3. Note the service URL (e.g., `https://ecommerce-backend.onrender.com`)

## Step 3: Vercel Frontend Deployment

### 3.1 Create Vercel Project
1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Click "New Project"
3. Import your GitHub repository
4. Select the repository

### 3.2 Configure Build Settings
- **Framework Preset**: Vite
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `dist`

### 3.3 Set Environment Variables
Add these environment variables in Vercel:

```
VITE_API_BASE_URL=https://your-render-app.onrender.com/api
VITE_IMAGE_BASE_URL=https://your-render-app.onrender.com
VITE_APP_NAME=E-commerce Store
VITE_APP_DESCRIPTION=Modern e-commerce platform
```

### 3.4 Deploy
1. Click "Deploy"
2. Vercel will build and deploy your frontend
3. Note the deployment URL (e.g., `https://your-app.vercel.app`)

## Step 4: Update Configuration

### 4.1 Update Backend CORS
Update the `ALLOWED_ORIGINS` environment variable in Render:
```
ALLOWED_ORIGINS=https://your-vercel-app.vercel.app,http://localhost:3000
```

### 4.2 Update Frontend API URL
Update the `VITE_API_BASE_URL` environment variable in Vercel:
```
VITE_API_BASE_URL=https://your-render-app.onrender.com/api
```

## Step 5: Database Seeding

### 5.1 Upload Product Images
1. Upload your product images to a cloud storage service (AWS S3, Cloudinary, etc.)
2. Update image URLs in the database to point to cloud storage
3. Or use the existing local paths if images are included in the Docker image

### 5.2 Seed Database
The backend will automatically seed the database on first startup with the `DataInitializer` component.

## Step 6: Image Storage Strategy

### Option 1: Cloud Storage (Recommended)
1. Upload images to AWS S3, Cloudinary, or similar
2. Update image URLs in database to cloud URLs
3. Update frontend to use cloud URLs directly

### Option 2: Render Static Files
1. Include images in Docker image (current setup)
2. Images served from Render backend
3. Update image paths to use Render domain

## Step 7: Domain Configuration

### 7.1 Custom Domain (Optional)
1. Configure custom domain in Vercel
2. Update CORS settings in Render
3. Update environment variables

### 7.2 SSL/HTTPS
- Vercel provides automatic SSL
- Render provides automatic SSL
- Ensure all URLs use HTTPS

## Step 8: Monitoring and Maintenance

### 8.1 Health Checks
- Backend health endpoint: `https://your-render-app.onrender.com/api/health`
- Frontend health: Check Vercel deployment status

### 8.2 Logs
- Render: View logs in Render dashboard
- Vercel: View logs in Vercel dashboard
- MongoDB Atlas: View logs in Atlas dashboard

## Troubleshooting

### Common Issues
1. **CORS Errors**: Check `ALLOWED_ORIGINS` in Render
2. **Database Connection**: Verify MongoDB Atlas connection string
3. **Image Loading**: Check image URLs and CORS settings
4. **Build Failures**: Check environment variables and dependencies

### Debug Steps
1. Check Render logs for backend issues
2. Check Vercel logs for frontend issues
3. Test API endpoints directly
4. Verify environment variables

## Security Considerations

1. **Environment Variables**: Never commit sensitive data
2. **Database Access**: Use least privilege principle
3. **CORS**: Restrict to known domains
4. **HTTPS**: Always use HTTPS in production
5. **Rate Limiting**: Consider implementing rate limiting

## Cost Optimization

1. **Render**: Use free tier for development
2. **Vercel**: Use free tier for development
3. **MongoDB Atlas**: Use M0 free tier for development
4. **Image Storage**: Use free tiers of cloud storage services

## Next Steps

1. Set up monitoring and alerting
2. Implement CI/CD pipelines
3. Add automated testing
4. Set up backup strategies
5. Consider scaling strategies
