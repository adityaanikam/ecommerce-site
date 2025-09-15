import React from 'react';
import { cn } from '@/utils/cn';

export interface GridProps extends React.HTMLAttributes<HTMLDivElement> {
  cols?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
  gap?: 'none' | 'sm' | 'md' | 'lg' | 'xl';
  responsive?: {
    sm?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
    md?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
    lg?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
    xl?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
  };
}

const colsClasses = {
  1: 'grid-cols-1',
  2: 'grid-cols-2',
  3: 'grid-cols-3',
  4: 'grid-cols-4',
  5: 'grid-cols-5',
  6: 'grid-cols-6',
  12: 'grid-cols-12',
};

const gapClasses = {
  none: 'gap-0',
  sm: 'gap-2',
  md: 'gap-4',
  lg: 'gap-6',
  xl: 'gap-8',
};

const responsiveColsClasses = {
  1: 'sm:grid-cols-1',
  2: 'sm:grid-cols-2',
  3: 'sm:grid-cols-3',
  4: 'sm:grid-cols-4',
  5: 'sm:grid-cols-5',
  6: 'sm:grid-cols-6',
  12: 'sm:grid-cols-12',
};

const responsiveMdClasses = {
  1: 'md:grid-cols-1',
  2: 'md:grid-cols-2',
  3: 'md:grid-cols-3',
  4: 'md:grid-cols-4',
  5: 'md:grid-cols-5',
  6: 'md:grid-cols-6',
  12: 'md:grid-cols-12',
};

const responsiveLgClasses = {
  1: 'lg:grid-cols-1',
  2: 'lg:grid-cols-2',
  3: 'lg:grid-cols-3',
  4: 'lg:grid-cols-4',
  5: 'lg:grid-cols-5',
  6: 'lg:grid-cols-6',
  12: 'lg:grid-cols-12',
};

const responsiveXlClasses = {
  1: 'xl:grid-cols-1',
  2: 'xl:grid-cols-2',
  3: 'xl:grid-cols-3',
  4: 'xl:grid-cols-4',
  5: 'xl:grid-cols-5',
  6: 'xl:grid-cols-6',
  12: 'xl:grid-cols-12',
};

export const Grid: React.FC<GridProps> = ({
  className,
  cols = 1,
  gap = 'md',
  responsive,
  children,
  ...props
}) => {
  const responsiveClasses = responsive
    ? [
        responsive.sm && responsiveColsClasses[responsive.sm],
        responsive.md && responsiveMdClasses[responsive.md],
        responsive.lg && responsiveLgClasses[responsive.lg],
        responsive.xl && responsiveXlClasses[responsive.xl],
      ].filter(Boolean)
    : [];

  return (
    <div
      className={cn(
        'grid',
        colsClasses[cols],
        gapClasses[gap],
        responsiveClasses,
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

// Grid Item component for more control
export interface GridItemProps extends React.HTMLAttributes<HTMLDivElement> {
  colSpan?: 1 | 2 | 3 | 4 | 5 | 6 | 12;
  rowSpan?: 1 | 2 | 3 | 4 | 5 | 6;
  responsive?: {
    sm?: { colSpan?: 1 | 2 | 3 | 4 | 5 | 6 | 12; rowSpan?: 1 | 2 | 3 | 4 | 5 | 6 };
    md?: { colSpan?: 1 | 2 | 3 | 4 | 5 | 6 | 12; rowSpan?: 1 | 2 | 3 | 4 | 5 | 6 };
    lg?: { colSpan?: 1 | 2 | 3 | 4 | 5 | 6 | 12; rowSpan?: 1 | 2 | 3 | 4 | 5 | 6 };
    xl?: { colSpan?: 1 | 2 | 3 | 4 | 5 | 6 | 12; rowSpan?: 1 | 2 | 3 | 4 | 5 | 6 };
  };
}

const colSpanClasses = {
  1: 'col-span-1',
  2: 'col-span-2',
  3: 'col-span-3',
  4: 'col-span-4',
  5: 'col-span-5',
  6: 'col-span-6',
  12: 'col-span-12',
};

const rowSpanClasses = {
  1: 'row-span-1',
  2: 'row-span-2',
  3: 'row-span-3',
  4: 'row-span-4',
  5: 'row-span-5',
  6: 'row-span-6',
};

export const GridItem: React.FC<GridItemProps> = ({
  className,
  colSpan,
  rowSpan,
  responsive,
  children,
  ...props
}) => {
  const responsiveClasses = responsive
    ? [
        responsive.sm?.colSpan && `sm:col-span-${responsive.sm.colSpan}`,
        responsive.sm?.rowSpan && `sm:row-span-${responsive.sm.rowSpan}`,
        responsive.md?.colSpan && `md:col-span-${responsive.md.colSpan}`,
        responsive.md?.rowSpan && `md:row-span-${responsive.md.rowSpan}`,
        responsive.lg?.colSpan && `lg:col-span-${responsive.lg.colSpan}`,
        responsive.lg?.rowSpan && `lg:row-span-${responsive.lg.rowSpan}`,
        responsive.xl?.colSpan && `xl:col-span-${responsive.xl.colSpan}`,
        responsive.xl?.rowSpan && `xl:row-span-${responsive.xl.rowSpan}`,
      ].filter(Boolean)
    : [];

  return (
    <div
      className={cn(
        colSpan && colSpanClasses[colSpan],
        rowSpan && rowSpanClasses[rowSpan],
        responsiveClasses,
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};
