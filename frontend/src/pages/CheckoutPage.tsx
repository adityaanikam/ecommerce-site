import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, CreditCard, Truck, MapPin } from 'lucide-react';
import { Container, Button, Card, CardContent, Input, Tabs, TabsList, TabsTrigger, TabsContent } from '@/components';
import { useCart } from '@/contexts/CartContext';
import { Address } from '@/types/api';

export const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const { state: cartState } = useCart();
  const [shippingAddress, setShippingAddress] = React.useState<Address>({
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: '',
  });

  const subtotal = cartState.totalAmount;
  const shipping = subtotal >= 50 ? 0 : 4.99;
  const tax = subtotal * 0.1;
  const total = subtotal + shipping + tax;

  const handleAddressChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setShippingAddress(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Handle order submission
    console.log('Order submitted:', {
      items: cartState.items,
      shippingAddress,
      total,
    });
  };

  if (cartState.items.length === 0) {
    return (
      <Container className="py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Your cart is empty</h2>
          <Button onClick={() => navigate('/products')}>Continue Shopping</Button>
        </div>
      </Container>
    );
  }

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        <Button
          variant="ghost"
          onClick={() => navigate(-1)}
          className="mb-6"
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Back
        </Button>

        <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-8">
            Checkout
          </h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2">
            <div className="space-y-6">
              {/* Shipping Address */}
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center gap-3 mb-6">
                    <MapPin className="h-5 w-5 text-primary-500" />
                    <h2 className="text-xl font-semibold">Shipping Address</h2>
                  </div>

                  <form className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input
                      name="street"
                      placeholder="Street Address"
                      value={shippingAddress.street}
                      onChange={handleAddressChange}
                      className="md:col-span-2"
                    />
                    <Input
                      name="city"
                      placeholder="City"
                      value={shippingAddress.city}
                      onChange={handleAddressChange}
                    />
                    <Input
                      name="state"
                      placeholder="State"
                      value={shippingAddress.state}
                      onChange={handleAddressChange}
                    />
                    <Input
                      name="zipCode"
                      placeholder="ZIP Code"
                      value={shippingAddress.zipCode}
                      onChange={handleAddressChange}
                    />
                    <Input
                      name="country"
                      placeholder="Country"
                      value={shippingAddress.country}
                      onChange={handleAddressChange}
                    />
                  </form>
                </CardContent>
              </Card>

              {/* Shipping Method */}
            <Card>
              <CardContent className="p-6">
                  <div className="flex items-center gap-3 mb-6">
                    <Truck className="h-5 w-5 text-primary-500" />
                    <h2 className="text-xl font-semibold">Shipping Method</h2>
                  </div>

                  <div className="space-y-4">
                    <label className="flex items-center justify-between p-4 border border-secondary-200 dark:border-secondary-700 rounded-lg cursor-pointer hover:border-primary-500 transition-colors">
                      <div className="flex items-center gap-3">
                        <input
                          type="radio"
                          name="shipping"
                          className="text-primary-600 focus:ring-primary-500"
                          defaultChecked
                        />
                        <div>
                          <p className="font-medium">Standard Shipping</p>
                          <p className="text-sm text-secondary-500">3-5 business days</p>
                        </div>
                      </div>
                      <span className="font-medium">
                        {subtotal >= 50 ? 'Free' : '$4.99'}
                      </span>
                    </label>

                    <label className="flex items-center justify-between p-4 border border-secondary-200 dark:border-secondary-700 rounded-lg cursor-pointer hover:border-primary-500 transition-colors">
                      <div className="flex items-center gap-3">
                        <input
                          type="radio"
                          name="shipping"
                          className="text-primary-600 focus:ring-primary-500"
                        />
                  <div>
                          <p className="font-medium">Express Shipping</p>
                          <p className="text-sm text-secondary-500">1-2 business days</p>
                        </div>
                    </div>
                      <span className="font-medium">$14.99</span>
                    </label>
                  </div>
                </CardContent>
              </Card>

              {/* Payment Method */}
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-center gap-3 mb-6">
                    <CreditCard className="h-5 w-5 text-primary-500" />
                    <h2 className="text-xl font-semibold">Payment Method</h2>
                  </div>

                  <Tabs defaultValue="card">
                    <TabsList>
                      <TabsTrigger value="card">Credit Card</TabsTrigger>
                      <TabsTrigger value="paypal">PayPal</TabsTrigger>
                    </TabsList>

                    <TabsContent value="card" className="mt-4">
                      <form className="space-y-4">
                        <Input placeholder="Card Number" />
                        <div className="grid grid-cols-3 gap-4">
                          <Input placeholder="MM/YY" className="col-span-1" />
                          <Input placeholder="CVC" className="col-span-1" />
                        </div>
                        <Input placeholder="Name on Card" />
                      </form>
                    </TabsContent>

                    <TabsContent value="paypal" className="mt-4">
                      <div className="text-center py-8">
                        <p className="text-secondary-600 dark:text-secondary-400">
                          You will be redirected to PayPal to complete your payment.
                        </p>
                      </div>
                    </TabsContent>
                  </Tabs>
              </CardContent>
            </Card>
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <Card className="sticky top-8">
              <CardContent className="p-6">
                <h2 className="text-xl font-semibold mb-6">Order Summary</h2>

                {/* Items */}
              <div className="space-y-4 mb-6">
                  {cartState.items.map((item) => (
                    <div key={item.productId} className="flex items-center gap-4">
                      <img
                        src={item.image}
                        alt={item.name}
                        className="w-16 h-16 rounded-lg object-cover"
                      />
                      <div className="flex-1">
                        <h3 className="font-medium text-secondary-900 dark:text-secondary-100">
                          {item.name}
                        </h3>
                        <p className="text-sm text-secondary-500">
                          Qty: {item.quantity}
                        </p>
                      </div>
                      <p className="font-medium">
                        ${(item.price * item.quantity).toFixed(2)}
                      </p>
                </div>
                  ))}
                </div>
                
                {/* Summary */}
                <div className="space-y-3 py-6 border-y border-secondary-200 dark:border-secondary-700">
                <div className="flex justify-between">
                    <span className="text-secondary-600 dark:text-secondary-400">Subtotal</span>
                    <span className="font-medium">${subtotal.toFixed(2)}</span>
                </div>
                  <div className="flex justify-between">
                    <span className="text-secondary-600 dark:text-secondary-400">Shipping</span>
                    <span className="font-medium">
                      {shipping === 0 ? 'Free' : `$${shipping.toFixed(2)}`}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-secondary-600 dark:text-secondary-400">Tax</span>
                    <span className="font-medium">${tax.toFixed(2)}</span>
                  </div>
                </div>

                <div className="flex justify-between items-center mb-6">
                  <span className="text-lg font-semibold">Total</span>
                  <span className="text-2xl font-bold text-primary-600">
                    ${total.toFixed(2)}
                  </span>
              </div>

                <Button
                  size="lg"
                  className="w-full"
                  onClick={handleSubmit}
                >
                  Place Order
                </Button>

                <p className="text-xs text-center text-secondary-500 mt-4">
                  By placing your order, you agree to our Terms of Service and Privacy Policy
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </Container>
    </div>
  );
};

export default CheckoutPage;