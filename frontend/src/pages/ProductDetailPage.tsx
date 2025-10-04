import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Minus, Plus, ShoppingCart, Heart } from 'lucide-react';
import { Button, Container, Card, CardContent, Badge, Tabs, TabsList, TabsTrigger, TabsContent } from '@/components';
import { ProductGallery, ProductSpecs, AddToCartAnimation } from '@/components/ecommerce';
import { useCart } from '@/contexts/CartContext';
import { productService } from '@/services/productService';
import { Product } from '@/types/api';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const [product, setProduct] = React.useState<Product | null>(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);
  const [quantity, setQuantity] = React.useState(1);
  const [showAddToCartAnimation, setShowAddToCartAnimation] = React.useState(false);

  React.useEffect(() => {
    const fetchProduct = async () => {
      if (!id) return;
      
      setIsLoading(true);
      setError(null);
      
      try {
        const productData = await productService.getProductById(id);
        setProduct(productData);
      } catch (err) {
        setError('Product not found');
        console.error('Failed to fetch product:', err instanceof Error ? err.message : String(err));
      } finally {
        setIsLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
        <Container className="py-8">
          <div className="animate-pulse">
            <div className="h-8 bg-secondary-200 rounded w-1/4 mb-6"></div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="h-96 bg-secondary-200 rounded"></div>
              <div className="space-y-4">
                <div className="h-8 bg-secondary-200 rounded w-3/4"></div>
                <div className="h-4 bg-secondary-200 rounded w-1/2"></div>
                <div className="h-4 bg-secondary-200 rounded w-1/3"></div>
                <div className="h-12 bg-secondary-200 rounded w-1/4"></div>
              </div>
            </div>
          </div>
        </Container>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
        <Container className="py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-secondary-900 dark:text-secondary-100 mb-4">
              Product Not Found
            </h1>
            <p className="text-secondary-600 dark:text-secondary-400 mb-6">
              The product you're looking for doesn't exist or has been removed.
            </p>
            <Button onClick={() => navigate('/products')}>
              Back to Products
            </Button>
          </div>
        </Container>
      </div>
    );
  }

  const handleAddToCart = () => {
    if (product) {
      addToCart(product);
      setShowAddToCartAnimation(true);
      setTimeout(() => setShowAddToCartAnimation(false), 2000);
    }
  };

  const handleQuantityChange = (newQuantity: number) => {
    if (newQuantity >= 1 && newQuantity <= (product?.stock || 1)) {
      setQuantity(newQuantity);
    }
  };

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        {/* Breadcrumb */}
        <div className="flex items-center space-x-2 text-sm text-secondary-600 dark:text-secondary-400 mb-6">
          <button 
            onClick={() => navigate('/products')}
            className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
          >
            Products
          </button>
          <ChevronRight className="h-4 w-4" />
          <span className="text-secondary-900 dark:text-secondary-100">{product.name}</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Product Images */}
          <div className="space-y-4">
            <ProductGallery images={product.images} product={product} />
          </div>

          {/* Product Info */}
          <div className="space-y-6">
            <div>
              <div className="flex items-center space-x-2 mb-2">
                <Badge variant="secondary" className="text-xs">
                  {product.category}
                </Badge>
                {product.subcategory && (
                  <Badge variant="outline" className="text-xs">
                    {product.subcategory}
                  </Badge>
                )}
              </div>
              
              <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-2">
                {product.name}
              </h1>
              
              <p className="text-lg text-secondary-600 dark:text-secondary-400 mb-4">
                by {product.brand}
              </p>

              <div className="flex items-center space-x-4 mb-6">
                <div className="flex items-center space-x-2">
                  <div className="flex items-center">
                    {[...Array(5)].map((_, i) => (
                      <svg
                        key={i}
                        className={`h-5 w-5 ${
                          i < Math.floor(product.rating || 0)
                            ? 'text-yellow-400'
                            : 'text-secondary-300 dark:text-secondary-600'
                        }`}
                        fill="currentColor"
                        viewBox="0 0 20 20"
                      >
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                      </svg>
                    ))}
                  </div>
                  <span className="text-sm text-secondary-600 dark:text-secondary-400">
                    {product.rating?.toFixed(1)} ({product.stock} reviews)
                  </span>
                </div>
              </div>

              <div className="flex items-center space-x-4 mb-6">
                <span className="text-3xl font-bold text-primary-600 dark:text-primary-400">
                  ${product.price?.toFixed(2)}
                </span>
                {product.discountPrice && product.discountPrice < product.price && (
                  <span className="text-xl text-secondary-500 line-through">
                    ${product.price?.toFixed(2)}
                  </span>
                )}
                {product.discountPrice && product.discountPrice < product.price && (
                  <Badge variant="destructive" className="text-sm">
                    Save ${(product.price - product.discountPrice).toFixed(2)}
                  </Badge>
                )}
              </div>

              <p className="text-secondary-700 dark:text-secondary-300 leading-relaxed mb-6">
                {product.description}
              </p>
            </div>

            {/* Quantity and Add to Cart */}
            <div className="space-y-4">
              <div className="flex items-center space-x-4">
                <span className="text-sm font-medium text-secondary-700 dark:text-secondary-300">
                  Quantity:
                </span>
                <div className="flex items-center border border-secondary-300 dark:border-secondary-600 rounded-lg">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleQuantityChange(quantity - 1)}
                    disabled={quantity <= 1}
                    className="h-8 w-8 p-0"
                  >
                    <Minus className="h-4 w-4" />
                  </Button>
                  <span className="px-4 py-2 text-sm font-medium min-w-[3rem] text-center">
                    {quantity}
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleQuantityChange(quantity + 1)}
                    disabled={quantity >= (product.stock || 1)}
                    className="h-8 w-8 p-0"
                  >
                    <Plus className="h-4 w-4" />
                  </Button>
                </div>
                <span className="text-sm text-secondary-600 dark:text-secondary-400">
                  {product.stock} in stock
                </span>
              </div>

              <div className="flex space-x-4">
                <Button
                  size="lg"
                  onClick={handleAddToCart}
                  disabled={!product.stock || product.stock <= 0}
                  className="flex-1"
                >
                  <ShoppingCart className="h-5 w-5 mr-2" />
                  Add to Cart
                </Button>
                <Button
                  variant="outline"
                  size="lg"
                  className="px-4"
                >
                  <Heart className="h-5 w-5" />
                </Button>
              </div>

              {showAddToCartAnimation && (
                <AddToCartAnimation />
              )}
            </div>
          </div>
        </div>

        {/* Product Details Tabs */}
        <div className="mt-12">
          <Tabs defaultValue="specifications" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="specifications">Specifications</TabsTrigger>
              <TabsTrigger value="description">Description</TabsTrigger>
              <TabsTrigger value="reviews">Reviews</TabsTrigger>
            </TabsList>
            
            <TabsContent value="specifications" className="mt-6">
              <Card>
                <CardContent className="p-6">
                  <ProductSpecs specs={product.specs || {}} />
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="description" className="mt-6">
              <Card>
                <CardContent className="p-6">
                  <div className="prose dark:prose-invert max-w-none">
                    <p className="text-secondary-700 dark:text-secondary-300 leading-relaxed">
                      {product.description}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="reviews" className="mt-6">
              <Card>
                <CardContent className="p-6">
                  <div className="text-center py-8">
                    <p className="text-secondary-600 dark:text-secondary-400">
                      Reviews feature coming soon!
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </Container>
    </div>
  );
};

export default ProductDetailPage;