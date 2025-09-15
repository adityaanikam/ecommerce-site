import React from 'react';
import { CreditCard, Truck, CheckCircle, ArrowRight, ArrowLeft, Lock } from 'lucide-react';
import { Container, Button, Card, CardContent } from '@/components';

export const CheckoutPage: React.FC = () => {
  const [currentStep, setCurrentStep] = React.useState(1);

  const steps = [
    { id: 1, title: 'Shipping', icon: Truck },
    { id: 2, title: 'Payment', icon: CreditCard },
    { id: 3, title: 'Review', icon: CheckCircle }
  ];

  return (
    <div className="min-h-screen bg-secondary-50 dark:bg-secondary-900">
      <Container className="py-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-secondary-900 dark:text-secondary-100 mb-2">
            Checkout
          </h1>
          <p className="text-secondary-600 dark:text-secondary-400">
            Complete your order securely
          </p>
        </div>

        {/* Progress Steps */}
        <div className="flex items-center justify-center mb-12">
          {steps.map((step, index) => {
            const Icon = step.icon;
            const isActive = currentStep === step.id;
            const isCompleted = currentStep > step.id;
            
            return (
              <div key={step.id} className="flex items-center">
                <div className={`flex items-center justify-center w-10 h-10 rounded-full border-2 ${
                  isCompleted 
                    ? 'bg-success-600 border-success-600 text-white' 
                    : isActive 
                      ? 'bg-primary-600 border-primary-600 text-white' 
                      : 'bg-white dark:bg-secondary-800 border-secondary-300 dark:border-secondary-600 text-secondary-400'
                }`}>
                  {isCompleted ? (
                    <CheckCircle className="h-5 w-5" />
                  ) : (
                    <Icon className="h-5 w-5" />
                  )}
                </div>
                <span className={`ml-2 text-sm font-medium ${
                  isActive ? 'text-primary-600' : 'text-secondary-500'
                }`}>
                  {step.title}
                </span>
                {index < steps.length - 1 && (
                  <div className={`w-16 h-0.5 mx-4 ${
                    isCompleted ? 'bg-success-600' : 'bg-secondary-300 dark:bg-secondary-600'
                  }`} />
                )}
              </div>
            );
          })}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2">
            <Card>
              <CardContent className="p-6">
                {currentStep === 1 && (
                  <div>
                    <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-6">
                      Shipping Information
                    </h2>
                    <p className="text-secondary-600 dark:text-secondary-400 mb-6">
                      Shipping form will be implemented here
                    </p>
                    <Button onClick={() => setCurrentStep(2)} size="lg">
                      Continue to Payment
                      <ArrowRight className="ml-2 h-5 w-5" />
                    </Button>
                  </div>
                )}

                {currentStep === 2 && (
                  <div>
                    <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-6">
                      Payment Information
                    </h2>
                    <p className="text-secondary-600 dark:text-secondary-400 mb-6">
                      Payment form will be implemented here
                    </p>
                    <div className="flex gap-4">
                      <Button variant="outline" onClick={() => setCurrentStep(1)} size="lg">
                        <ArrowLeft className="mr-2 h-5 w-5" />
                        Back to Shipping
                      </Button>
                      <Button onClick={() => setCurrentStep(3)} size="lg">
                        Review Order
                        <ArrowRight className="ml-2 h-5 w-5" />
                      </Button>
                    </div>
                  </div>
                )}

                {currentStep === 3 && (
                  <div>
                    <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-6">
                      Review Your Order
                    </h2>
                    <p className="text-secondary-600 dark:text-secondary-400 mb-6">
                      Order review will be implemented here
                    </p>
                    <div className="flex gap-4">
                      <Button variant="outline" onClick={() => setCurrentStep(2)} size="lg">
                        <ArrowLeft className="mr-2 h-5 w-5" />
                        Back to Payment
                      </Button>
                      <Button size="lg">
                        <Lock className="mr-2 h-5 w-5" />
                        Place Order
                      </Button>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Order Summary Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white dark:bg-secondary-800 rounded-lg shadow-sm p-6 sticky top-8">
              <h2 className="text-xl font-semibold text-secondary-900 dark:text-secondary-100 mb-6">
                Order Summary
              </h2>

              <div className="space-y-4 mb-6">
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Subtotal</span>
                  <span className="font-medium text-secondary-900 dark:text-secondary-100">
                    $449.98
                  </span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Shipping</span>
                  <span className="font-medium text-success-600">FREE</span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-secondary-600 dark:text-secondary-400">Tax</span>
                  <span className="font-medium text-secondary-900 dark:text-secondary-100">
                    $36.00
                  </span>
                </div>
                
                <div className="border-t border-secondary-200 dark:border-secondary-700 pt-4">
                  <div className="flex justify-between">
                    <span className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                      Total
                    </span>
                    <span className="text-lg font-semibold text-secondary-900 dark:text-secondary-100">
                      $485.98
                    </span>
                  </div>
                </div>
              </div>

              <div className="text-center text-sm text-secondary-600 dark:text-secondary-400">
                <Lock className="h-4 w-4 inline mr-1" />
                Secure checkout with SSL encryption
              </div>
            </div>
          </div>
        </div>
      </Container>
    </div>
  );
};
