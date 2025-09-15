import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient();

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <div className="min-h-screen bg-gray-100">
          <h1 className="text-3xl font-bold text-center py-8">
            E-commerce Application
          </h1>
        </div>
      </Router>
    </QueryClientProvider>
  );
}

export default App;