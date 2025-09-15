# E-commerce Frontend

A modern React e-commerce frontend built with TypeScript, Vite, and Tailwind CSS.

## Features

- ⚡ **Vite** - Fast build tool and dev server
- ⚛️ **React 18** - Latest React with concurrent features
- 🔷 **TypeScript** - Type-safe development
- 🎨 **Tailwind CSS** - Utility-first CSS framework
- 🛣️ **React Router v6** - Client-side routing
- 🔄 **React Query** - Server state management
- 📝 **React Hook Form** - Form handling with validation
- 🎯 **Context API** - Client state management
- 🔐 **JWT Authentication** - Secure authentication
- 📱 **Responsive Design** - Mobile-first approach
- 🎭 **Dark Mode** - Theme switching
- 🚀 **PWA Ready** - Progressive Web App features

## Tech Stack

- **Frontend**: React 18, TypeScript, Vite
- **Styling**: Tailwind CSS, Headless UI
- **State Management**: React Query, Context API
- **Routing**: React Router v6
- **Forms**: React Hook Form, Zod validation
- **HTTP Client**: Axios
- **Icons**: Lucide React
- **Notifications**: React Hot Toast
- **PWA**: Vite PWA Plugin

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── ui/             # Basic UI components
│   ├── layout/         # Layout components
│   └── forms/          # Form components
├── pages/              # Page components
├── hooks/              # Custom React hooks
├── services/           # API services
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── contexts/           # React contexts
└── assets/             # Static assets
```

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

1. Clone the repository
2. Install dependencies:
   ```bash
   npm install
   ```

3. Copy environment variables:
   ```bash
   cp env.example .env
   ```

4. Update environment variables in `.env`:
   ```env
   VITE_API_URL=http://localhost:8080/api
   VITE_APP_NAME=E-commerce Store
   VITE_APP_VERSION=1.0.0
   ```

### Development

Start the development server:

```bash
npm run dev
```

The app will be available at `http://localhost:3000`.

### Building for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

### Preview Production Build

```bash
npm run preview
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## API Integration

The frontend integrates with the Spring Boot backend API. Make sure the backend is running on `http://localhost:8080` (or update the `VITE_API_URL` in your `.env` file).

### Authentication

The app uses JWT tokens for authentication. Tokens are automatically included in API requests and refreshed when needed.

### State Management

- **Server State**: Managed by React Query for caching, synchronization, and background updates
- **Client State**: Managed by React Context for user authentication, cart, and theme

## Features

### User Authentication
- Login/Register with email and password
- OAuth2 Google login
- Password reset functionality
- Protected routes

### Product Management
- Product listing with search and filters
- Product detail pages
- Product reviews and ratings
- Image gallery

### Shopping Cart
- Add/remove items
- Update quantities
- Persistent cart across sessions
- Cart validation

### Order Management
- Checkout process
- Order history
- Order tracking
- Order status updates

### User Profile
- Profile management
- Address management
- Order history
- Account settings

## Styling

The app uses Tailwind CSS with a custom design system:

- **Colors**: Primary, secondary, success, warning, error
- **Typography**: Inter font family
- **Components**: Custom component library
- **Dark Mode**: Theme switching support

## Performance

- **Code Splitting**: Automatic route-based code splitting
- **Lazy Loading**: Components loaded on demand
- **Image Optimization**: Optimized image loading
- **Caching**: React Query for API response caching
- **Bundle Analysis**: Built-in bundle analyzer

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.