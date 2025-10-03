import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';
import { cn } from '@/utils/cn';

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface BreadcrumbsProps {
  items: BreadcrumbItem[];
  className?: string;
}

export const Breadcrumbs: React.FC<BreadcrumbsProps> = ({
  items,
  className,
}) => {
  return (
    <nav
      className={cn(
        'flex items-center space-x-2 text-sm text-secondary-600 dark:text-secondary-400',
        className
      )}
    >
      <Link
        to="/"
        className="flex items-center hover:text-secondary-900 dark:hover:text-secondary-100 transition-colors"
      >
        <Home className="h-4 w-4" />
      </Link>

      {items.map((item, index) => (
        <React.Fragment key={index}>
          <ChevronRight className="h-4 w-4 text-secondary-400" />
          {item.href ? (
            <Link
              to={item.href}
              className="hover:text-secondary-900 dark:hover:text-secondary-100 transition-colors"
            >
              {item.label}
            </Link>
          ) : (
            <span className="text-secondary-900 dark:text-secondary-100 font-medium">
              {item.label}
            </span>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
};
