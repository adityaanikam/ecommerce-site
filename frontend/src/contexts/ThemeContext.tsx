import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Theme, ThemeContextType } from '@/types';

// Default theme
const defaultTheme: Theme = {
  name: 'light',
  colors: {
    primary: '#3b82f6',
    secondary: '#64748b',
    success: '#22c55e',
    warning: '#f59e0b',
    error: '#ef4444',
    background: '#ffffff',
    surface: '#f8fafc',
    text: '#0f172a',
    textSecondary: '#64748b',
  },
};

// Dark theme
const darkTheme: Theme = {
  name: 'dark',
  colors: {
    primary: '#60a5fa',
    secondary: '#94a3b8',
    success: '#4ade80',
    warning: '#fbbf24',
    error: '#f87171',
    background: '#0f172a',
    surface: '#1e293b',
    text: '#f8fafc',
    textSecondary: '#cbd5e1',
  },
};

// Create context
const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

// Theme provider component
interface ThemeProviderProps {
  children: ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [theme, setTheme] = useState<Theme>(defaultTheme);
  const [isDark, setIsDark] = useState(false);

  // Load theme from localStorage on mount
  useEffect(() => {
    const savedTheme = localStorage.getItem('theme');
    const savedIsDark = localStorage.getItem('isDark') === 'true';
    
    if (savedTheme && savedIsDark) {
      setTheme(darkTheme);
      setIsDark(true);
      document.documentElement.classList.add('dark');
    } else {
      setTheme(defaultTheme);
      setIsDark(false);
      document.documentElement.classList.remove('dark');
    }
  }, []);

  // Update CSS variables when theme changes
  useEffect(() => {
    const root = document.documentElement;
    
    // Update CSS custom properties
    root.style.setProperty('--color-primary', theme.colors.primary);
    root.style.setProperty('--color-secondary', theme.colors.secondary);
    root.style.setProperty('--color-success', theme.colors.success);
    root.style.setProperty('--color-warning', theme.colors.warning);
    root.style.setProperty('--color-error', theme.colors.error);
    root.style.setProperty('--color-background', theme.colors.background);
    root.style.setProperty('--color-surface', theme.colors.surface);
    root.style.setProperty('--color-text', theme.colors.text);
    root.style.setProperty('--color-text-secondary', theme.colors.textSecondary);
  }, [theme]);

  // Toggle theme
  const toggleTheme = (): void => {
    const newIsDark = !isDark;
    const newTheme = newIsDark ? darkTheme : defaultTheme;
    
    setTheme(newTheme);
    setIsDark(newIsDark);
    
    // Update localStorage
    localStorage.setItem('theme', newTheme.name);
    localStorage.setItem('isDark', newIsDark.toString());
    
    // Update document class
    if (newIsDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  // Set specific theme
  const setSpecificTheme = (newTheme: Theme): void => {
    const newIsDark = newTheme.name === 'dark';
    
    setTheme(newTheme);
    setIsDark(newIsDark);
    
    // Update localStorage
    localStorage.setItem('theme', newTheme.name);
    localStorage.setItem('isDark', newIsDark.toString());
    
    // Update document class
    if (newIsDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  // Context value
  const value: ThemeContextType = {
    theme,
    isDark,
    toggleTheme,
    setTheme: setSpecificTheme,
  };

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};

// Custom hook to use theme context
export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

export default ThemeContext;
