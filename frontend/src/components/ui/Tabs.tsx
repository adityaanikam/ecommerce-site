import React from 'react';
import { cn } from '@/utils/cn';

interface TabsContextValue {
  value: string;
  setValue: (v: string) => void;
}

const TabsContext = React.createContext<TabsContextValue | null>(null);

export interface TabsProps {
  defaultValue: string;
  value?: string;
  onValueChange?: (value: string) => void;
  className?: string;
  children: React.ReactNode;
}

export const Tabs: React.FC<TabsProps> = ({
  defaultValue,
  value,
  onValueChange,
  className,
  children,
}) => {
  const [internal, setInternal] = React.useState(defaultValue);
  const current = value !== undefined ? value : internal;

  const setValue = (v: string) => {
    if (onValueChange) onValueChange(v);
    if (value === undefined) setInternal(v);
  };

  return (
    <TabsContext.Provider value={{ value: current, setValue }}>
      <div className={cn('w-full', className)}>{children}</div>
    </TabsContext.Provider>
  );
};

export const TabsList: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({
  className,
  children,
  ...props
}) => {
  return (
    <div
      role="tablist"
      className={cn(
        'inline-flex h-10 items-center justify-center rounded-lg bg-secondary-100 dark:bg-secondary-800 p-1 gap-1',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export interface TabsTriggerProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  value: string;
}

export const TabsTrigger: React.FC<TabsTriggerProps> = ({
  value,
  className,
  children,
  ...props
}) => {
  const ctx = React.useContext(TabsContext);
  if (!ctx) throw new Error('TabsTrigger must be used within Tabs');
  const isActive = ctx.value === value;

  return (
    <button
      role="tab"
      aria-selected={isActive}
      onClick={() => ctx.setValue(value)}
      className={cn(
        'px-3 h-8 rounded-md text-sm font-medium transition-colors',
        isActive
          ? 'bg-white dark:bg-secondary-700 text-secondary-900 dark:text-secondary-100 shadow'
          : 'text-secondary-600 dark:text-secondary-300 hover:text-secondary-900 dark:hover:text-secondary-100'
      , className)}
      {...props}
    >
      {children}
    </button>
  );
};

export interface TabsContentProps
  extends React.HTMLAttributes<HTMLDivElement> {
  value: string;
}

export const TabsContent: React.FC<TabsContentProps> = ({
  value,
  className,
  children,
  ...props
}) => {
  const ctx = React.useContext(TabsContext);
  if (!ctx) throw new Error('TabsContent must be used within Tabs');
  if (ctx.value !== value) return null;
  return (
    <div role="tabpanel" className={className} {...props}>
      {children}
    </div>
  );
};
