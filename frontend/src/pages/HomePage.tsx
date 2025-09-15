import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Star, ShoppingBag, Truck, Shield, Headphones } from 'lucide-react';
import { Container, Grid, Button, Card, CardContent, Badge, RatingStars, PriceDisplay } from '@/components';
import { ProductCard, CategoryCard } from '@/components/ecommerce';

// Mock data - in real app, this would come from API
const featuredProducts = [
  {
    id: '1',
    name: 'Wireless Bluetooth Headphones',
    brand: 'TechSound',
    price: 199.99,
    discountPrice: 149.99,
    images: ['/api/placeholder/300/300'],
    ratings: { average: 4.5, count: 128 },
    stock: 25,
    description: 'Premium wireless headphones with noise cancellation'
  },
  {
    id: '2',
    name: 'Smart Fitness Watch',
    brand: 'FitTech',
    price: 299.99,
    images: ['/api/placeholder/300/300'],
    ratings: { average: 4.8, count: 89 },
    stock: 15,
    description: 'Advanced fitness tracking with heart rate monitor'
  },
  {
    id: '3',
    name: 'Wireless Charging Pad',
    brand: 'PowerUp',
    price: 49.99,
    discountPrice: 39.99,
    images: ['/api/placeholder/300/300'],
    ratings: { average: 4.2, count: 67 },
    stock: 50,
    description: 'Fast wireless charging for all compatible devices'
  },
  {
    id: '4',
    name: 'Bluetooth Speaker',
    brand: 'SoundWave',
    price: 79.99,
    images: ['/api/placeholder/300/300'],
    ratings: { average: 4.6, count: 156 },
    stock: 30,
    description: 'Portable speaker with 360-degree sound'
  }
];

const categories = [
  {
    id: '1',
    name: 'Electronics',
    description: 'Latest gadgets and tech',
    imageUrl: '/api/placeholder/400/300',
    productCount: 245
  },
  {
    id: '2',
    name: 'Fashion',
    description: 'Trendy clothing and accessories',
    imageUrl: '/api/placeholder/400/300',
    productCount: 189
  },
  {
    id: '3',
    name: 'Home & Garden',
    description: 'Everything for your home',
    imageUrl: '/api/placeholder/400/300',
    productCount: 156
  },
  {
    id: '4',
    name: 'Sports',
    description: 'Fitness and outdoor gear',
    imageUrl: '/api/placeholder/400/300',
    productCount: 98
  }
];

const testimonials = [
  {
    id: '1',
    name: 'Sarah Johnson',
    role: 'Fashion Blogger',
    content: 'Amazing quality products and super fast delivery! I\'ve been shopping here for months and never disappointed.',
    rating: 5,
    avatar: '/api/placeholder/60/60'
  },
  {
    id: '2',
    name: 'Mike Chen',
    role: 'Tech Enthusiast',
    content: 'Great selection of electronics at competitive prices. The customer service is outstanding!',
    rating: 5,
    avatar: '/api/placeholder/60/60'
  },
  {
    id: '3',
    name: 'Emily Davis',
    role: 'Home Decorator',
    content: 'Beautiful home products that arrived exactly as described. Highly recommend this store!',
    rating: 5,
    avatar: '/api/placeholder/60/60'
  }
];

const features = [
  {
    icon: <Truck className="h-8 w-8" />,
    title: 'Free Shipping',
    description: 'Free shipping on orders over $50'
  },
  {
    icon: <Shield className="h-8 w-8" />,
    title: 'Secure Payment',
    description: '100% secure payment processing'
  },
  {
    icon: <Headphones className="h-8 w-8" />,
    title: '24/7 Support',
    description: 'Round-the-clock customer support'
  },
  {
    icon: <ShoppingBag className="h-8 w-8" />,
    title: 'Easy Returns',
    description: '30-day hassle-free returns'
  }
];

export const HomePage: React.FC = () => {
  const [email, setEmail] = React.useState('');

  const handleNewsletterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Handle newsletter subscription
    console.log('Newsletter subscription:', email);
    setEmail('');
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-r from-primary-600 to-primary-800 text-white overflow-hidden">
        <div className="absolute inset-0 bg-black/20"></div>
        <Container className="relative py-20 lg:py-32">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-8">
              <div className="space-y-4">
                <h1 className="text-4xl lg:text-6xl font-bold leading-tight">
                  Discover Amazing
                  <span className="block text-yellow-300">Products</span>
                </h1>
                <p className="text-xl text-primary-100 max-w-lg">
                  Shop the latest trends in electronics, fashion, home decor, and more. 
                  Quality products at unbeatable prices with fast, free shipping.
                </p>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-4">
                <Link to="/products">
                  <Button size="lg" variant="secondary" className="w-full sm:w-auto">
                    Shop Now
                    <ArrowRight className="ml-2 h-5 w-5" />
                  </Button>
                </Link>
                <Link to="/categories">
                  <Button size="lg" variant="outline" className="w-full sm:w-auto border-white text-white hover:bg-white hover:text-primary-600">
                    Browse Categories
                  </Button>
                </Link>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-3 gap-8 pt-8">
                <div className="text-center">
                  <div className="text-3xl font-bold">10K+</div>
                  <div className="text-primary-200">Happy Customers</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold">5K+</div>
                  <div className="text-primary-200">Products</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold">99%</div>
                  <div className="text-primary-200">Satisfaction</div>
                </div>
              </div>
            </div>

            <div className="relative">
              <div className="aspect-square bg-white/10 rounded-2xl backdrop-blur-sm p-8">
                <img
                  src="/api/placeholder/500/500"
                  alt="Featured Product"
                  className="w-full h-full object-cover rounded-xl"
                />
              </div>
              {/* Floating elements */}
              <div className="absolute -top-4 -right-4 bg-yellow-400 text-primary-900 px-4 py-2 rounded-full font-bold">
                -30% OFF
              </div>
            </div>
          </div>
        </Container>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-secondary-50 dark:bg-secondary-800">
        <Container>
          <Grid cols={4} gap="lg" responsive={{ sm: 2, md: 4 }}>
            {features.map((feature, index) => (
              <div key={index} className="text-center space-y-4">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 dark:bg-primary-900 rounded-full text-primary-600 dark:text-primary-400">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                  {feature.title}
                </h3>
                <p className="text-secondary-600 dark:text-secondary-400">
                  {feature.description}
                </p>
              </div>
            ))}
          </Grid>
        </Container>
      </section>

      {/* Featured Products Section */}
      <section className="py-20">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Featured Products
            </h2>
            <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
              Discover our handpicked selection of the best products, carefully chosen for quality and value.
            </p>
          </div>

          <Grid cols={4} gap="lg" responsive={{ sm: 1, md: 2, lg: 4 }}>
            {featuredProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={(id) => console.log('Add to cart:', id)}
                onAddToWishlist={(id) => console.log('Add to wishlist:', id)}
                onQuickView={(id) => console.log('Quick view:', id)}
              />
            ))}
          </Grid>

          <div className="text-center mt-12">
            <Link to="/products">
              <Button size="lg" variant="outline">
                View All Products
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </Link>
          </div>
        </Container>
      </section>

      {/* Categories Section */}
      <section className="py-20 bg-secondary-50 dark:bg-secondary-800">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Shop by Category
            </h2>
            <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
              Explore our wide range of categories and find exactly what you're looking for.
            </p>
          </div>

          <Grid cols={4} gap="lg" responsive={{ sm: 1, md: 2, lg: 4 }}>
            {categories.map((category) => (
              <CategoryCard
                key={category.id}
                category={category}
                productCount={category.productCount}
                variant="default"
              />
            ))}
          </Grid>
        </Container>
      </section>

      {/* Testimonials Section */}
      <section className="py-20">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              What Our Customers Say
            </h2>
            <p className="text-lg text-secondary-600 dark:text-secondary-400 max-w-2xl mx-auto">
              Don't just take our word for it. Here's what our satisfied customers have to say.
            </p>
          </div>

          <Grid cols={3} gap="lg" responsive={{ sm: 1, md: 3 }}>
            {testimonials.map((testimonial) => (
              <Card key={testimonial.id} className="h-full">
                <CardContent className="p-6">
                  <div className="space-y-4">
                    <RatingStars rating={testimonial.rating} size="sm" />
                    <p className="text-secondary-600 dark:text-secondary-400 italic">
                      "{testimonial.content}"
                    </p>
                    <div className="flex items-center space-x-3">
                      <img
                        src={testimonial.avatar}
                        alt={testimonial.name}
                        className="w-12 h-12 rounded-full object-cover"
                      />
                      <div>
                        <h4 className="font-semibold text-secondary-900 dark:text-secondary-100">
                          {testimonial.name}
                        </h4>
                        <p className="text-sm text-secondary-500 dark:text-secondary-400">
                          {testimonial.role}
                        </p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </Grid>
        </Container>
      </section>

      {/* Newsletter Section */}
      <section className="py-20 bg-primary-600 text-white">
        <Container>
          <div className="max-w-4xl mx-auto text-center">
            <h2 className="text-3xl lg:text-4xl font-bold mb-4">
              Stay Updated
            </h2>
            <p className="text-xl text-primary-100 mb-8">
              Subscribe to our newsletter and be the first to know about new products, 
              exclusive deals, and special offers.
            </p>
            
            <form onSubmit={handleNewsletterSubmit} className="max-w-md mx-auto">
              <div className="flex gap-4">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email"
                  className="flex-1 px-4 py-3 rounded-lg text-secondary-900 placeholder-secondary-500 focus:outline-none focus:ring-2 focus:ring-yellow-400"
                  required
                />
                <Button type="submit" variant="secondary" size="lg">
                  Subscribe
                </Button>
              </div>
            </form>
            
            <p className="text-sm text-primary-200 mt-4">
              We respect your privacy. Unsubscribe at any time.
            </p>
          </div>
        </Container>
      </section>
    </div>
  );
};