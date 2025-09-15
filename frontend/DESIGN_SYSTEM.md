# Design System Documentation

## Overview

This design system provides a comprehensive set of reusable components, utilities, and guidelines for building a modern e-commerce application with React, TypeScript, and Tailwind CSS.

## Design Principles

### 1. Consistency
- Unified color palette and typography
- Consistent spacing and sizing scales
- Standardized component patterns

### 2. Accessibility
- WCAG 2.1 AA compliance
- Keyboard navigation support
- Screen reader compatibility
- High contrast ratios

### 3. Responsiveness
- Mobile-first approach
- Flexible grid system
- Adaptive components

### 4. Performance
- Optimized animations
- Lazy loading support
- Minimal bundle size

## Color Palette

### Primary Colors
- **Primary 50**: `#eff6ff` - Lightest primary
- **Primary 100**: `#dbeafe` - Very light primary
- **Primary 200**: `#bfdbfe` - Light primary
- **Primary 300**: `#93c5fd` - Medium light primary
- **Primary 400**: `#60a5fa` - Medium primary
- **Primary 500**: `#3b82f6` - Base primary
- **Primary 600**: `#2563eb` - Medium dark primary
- **Primary 700**: `#1d4ed8` - Dark primary
- **Primary 800**: `#1e40af` - Very dark primary
- **Primary 900**: `#1e3a8a` - Darkest primary

### Secondary Colors
- **Secondary 50**: `#f8fafc` - Lightest secondary
- **Secondary 100**: `#f1f5f9` - Very light secondary
- **Secondary 200**: `#e2e8f0` - Light secondary
- **Secondary 300**: `#cbd5e1` - Medium light secondary
- **Secondary 400**: `#94a3b8` - Medium secondary
- **Secondary 500**: `#64748b` - Base secondary
- **Secondary 600**: `#475569` - Medium dark secondary
- **Secondary 700**: `#334155` - Dark secondary
- **Secondary 800**: `#1e293b` - Very dark secondary
- **Secondary 900**: `#0f172a` - Darkest secondary

### Status Colors
- **Success**: Green palette for positive actions
- **Warning**: Yellow/Orange palette for caution
- **Error**: Red palette for errors and destructive actions

## Typography

### Font Families
- **Primary**: Inter (Sans-serif)
- **Monospace**: JetBrains Mono

### Font Sizes
- **xs**: 0.75rem (12px)
- **sm**: 0.875rem (14px)
- **base**: 1rem (16px)
- **lg**: 1.125rem (18px)
- **xl**: 1.25rem (20px)
- **2xl**: 1.5rem (24px)
- **3xl**: 1.875rem (30px)
- **4xl**: 2.25rem (36px)
- **5xl**: 3rem (48px)
- **6xl**: 3.75rem (60px)

### Font Weights
- **Thin**: 100
- **Light**: 300
- **Normal**: 400
- **Medium**: 500
- **Semibold**: 600
- **Bold**: 700
- **Extrabold**: 800
- **Black**: 900

## Spacing Scale

- **0**: 0px
- **1**: 0.25rem (4px)
- **2**: 0.5rem (8px)
- **3**: 0.75rem (12px)
- **4**: 1rem (16px)
- **5**: 1.25rem (20px)
- **6**: 1.5rem (24px)
- **8**: 2rem (32px)
- **10**: 2.5rem (40px)
- **12**: 3rem (48px)
- **16**: 4rem (64px)
- **20**: 5rem (80px)
- **24**: 6rem (96px)
- **32**: 8rem (128px)

## Component Library

### Base UI Components

#### Button
```tsx
<Button variant="default" size="md">
  Click me
</Button>
```

**Variants:**
- `default` - Primary button
- `destructive` - Error/danger actions
- `outline` - Secondary actions
- `secondary` - Tertiary actions
- `ghost` - Subtle actions
- `link` - Text links
- `success` - Success actions
- `warning` - Warning actions

**Sizes:**
- `sm` - Small (32px height)
- `md` - Medium (40px height)
- `lg` - Large (48px height)
- `xl` - Extra large (56px height)
- `icon` - Icon only (40px)
- `icon-sm` - Small icon (32px)
- `icon-lg` - Large icon (48px)

#### Input
```tsx
<Input
  type="email"
  placeholder="Enter your email"
  leftIcon={<Mail className="h-5 w-5" />}
/>
```

#### Card
```tsx
<Card>
  <CardHeader>
    <CardTitle>Card Title</CardTitle>
    <CardDescription>Card description</CardDescription>
  </CardHeader>
  <CardContent>
    Card content goes here
  </CardContent>
</Card>
```

#### Badge
```tsx
<Badge variant="default">Badge</Badge>
```

**Variants:**
- `default` - Primary badge
- `secondary` - Secondary badge
- `success` - Success badge
- `warning` - Warning badge
- `error` - Error badge
- `outline` - Outlined badge

#### Modal
```tsx
<Modal
  isOpen={isOpen}
  onClose={() => setIsOpen(false)}
  title="Modal Title"
>
  Modal content
</Modal>
```

#### Dropdown
```tsx
<Dropdown
  options={options}
  value={selectedValue}
  onChange={handleChange}
  placeholder="Select an option"
/>
```

#### Avatar
```tsx
<Avatar
  src="/path/to/image.jpg"
  fallback="John Doe"
  size="md"
  status="online"
/>
```

#### Spinner
```tsx
<Spinner size="md" variant="default" text="Loading..." />
```

### Form Components

#### FormInput
```tsx
<FormInput
  label="Email"
  placeholder="Enter your email"
  error={errors.email?.message}
  required
/>
```

#### FormSelect
```tsx
<FormSelect
  label="Category"
  options={categoryOptions}
  value={selectedCategory}
  onChange={handleCategoryChange}
/>
```

#### FormTextarea
```tsx
<FormTextarea
  label="Description"
  placeholder="Enter description"
  rows={4}
/>
```

### Layout Components

#### Container
```tsx
<Container size="lg" padding="md">
  Content goes here
</Container>
```

**Sizes:**
- `sm` - Max width 768px
- `md` - Max width 1024px
- `lg` - Max width 1280px
- `xl` - Max width 1536px
- `full` - Full width

#### Grid
```tsx
<Grid cols={3} gap="md" responsive={{ sm: 1, md: 2, lg: 3 }}>
  <GridItem>Item 1</GridItem>
  <GridItem>Item 2</GridItem>
  <GridItem>Item 3</GridItem>
</Grid>
```

#### Sidebar
```tsx
<Sidebar
  isOpen={isOpen}
  onClose={() => setIsOpen(false)}
  title="Sidebar Title"
>
  Sidebar content
</Sidebar>
```

### E-commerce Components

#### ProductCard
```tsx
<ProductCard
  product={product}
  onAddToCart={handleAddToCart}
  onAddToWishlist={handleAddToWishlist}
  variant="default"
/>
```

**Variants:**
- `default` - Standard product card
- `compact` - Compact horizontal layout
- `detailed` - Detailed with more information

#### PriceDisplay
```tsx
<PriceDisplay
  price={99.99}
  discountPrice={79.99}
  currency="$"
  size="lg"
  showDiscount
/>
```

#### RatingStars
```tsx
<RatingStars
  rating={4.5}
  showNumber
  showCount
  count={128}
  interactive
  onRatingChange={handleRatingChange}
/>
```

#### CategoryCard
```tsx
<CategoryCard
  category={category}
  productCount={42}
  variant="default"
/>
```

#### CartItem
```tsx
<CartItem
  item={cartItem}
  onUpdateQuantity={handleUpdateQuantity}
  onRemove={handleRemove}
  variant="default"
/>
```

#### OrderCard
```tsx
<OrderCard
  order={order}
  variant="default"
  showActions
/>
```

### Authentication Components

#### LoginForm
```tsx
<LoginForm
  onSuccess={() => navigate('/')}
  onForgotPassword={() => setShowForgotPassword(true)}
/>
```

#### RegisterForm
```tsx
<RegisterForm
  onSuccess={() => navigate('/')}
  onLogin={() => setShowLogin(true)}
/>
```

#### GoogleLoginButton
```tsx
<GoogleLoginButton
  onSuccess={handleGoogleSuccess}
  onError={handleGoogleError}
/>
```

## Dark Mode

The design system includes comprehensive dark mode support:

### Implementation
```tsx
import { useTheme } from '@/contexts';

const { isDark, toggleTheme } = useTheme();
```

### Automatic Dark Mode
Components automatically adapt to dark mode using Tailwind's `dark:` prefix:

```tsx
<div className="bg-white dark:bg-secondary-900 text-secondary-900 dark:text-secondary-100">
  Content
</div>
```

## Animations

### Built-in Animations
- `fade-in` - Fade in effect
- `slide-up` - Slide up from bottom
- `slide-down` - Slide down from top
- `scale-in` - Scale in effect
- `bounce-in` - Bounce in effect
- `wave` - Wave animation for loading
- `shimmer` - Shimmer effect for loading

### Usage
```tsx
<div className="animate-fade-in">
  Content with fade in animation
</div>
```

## Accessibility Features

### Keyboard Navigation
- All interactive elements are keyboard accessible
- Tab order is logical and intuitive
- Focus indicators are clearly visible

### Screen Reader Support
- Proper ARIA labels and descriptions
- Semantic HTML structure
- Role attributes where needed

### Color Contrast
- All text meets WCAG 2.1 AA contrast requirements
- High contrast mode support
- Color is not the only way to convey information

## Responsive Design

### Breakpoints
- `sm`: 640px and up
- `md`: 768px and up
- `lg`: 1024px and up
- `xl`: 1280px and up
- `2xl`: 1536px and up

### Mobile-First Approach
All components are designed mobile-first and scale up:

```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
  Responsive grid
</div>
```

## Performance Optimizations

### Code Splitting
Components are designed for optimal code splitting and lazy loading.

### Bundle Size
- Tree-shakable components
- Minimal dependencies
- Optimized imports

### Loading States
- Skeleton screens for better perceived performance
- Loading spinners and progress indicators
- Optimistic updates where appropriate

## Usage Guidelines

### Component Composition
```tsx
// Good: Compose components
<Card>
  <CardHeader>
    <CardTitle>Product</CardTitle>
  </CardHeader>
  <CardContent>
    <ProductCard product={product} />
  </CardContent>
</Card>

// Avoid: Overriding component styles
<ProductCard className="!bg-red-500" />
```

### Consistent Spacing
```tsx
// Good: Use design system spacing
<div className="space-y-4 p-6">
  <h2 className="mb-4">Title</h2>
  <p className="mt-2">Content</p>
</div>

// Avoid: Custom spacing values
<div className="space-y-[13px] p-[23px]">
```

### Color Usage
```tsx
// Good: Use semantic color names
<Button variant="error">Delete</Button>
<Badge variant="success">Active</Badge>

// Avoid: Direct color values
<Button className="bg-red-500">Delete</Button>
```

## Best Practices

1. **Consistency**: Always use design system components
2. **Accessibility**: Test with keyboard and screen readers
3. **Performance**: Use loading states and optimize images
4. **Responsive**: Test on all device sizes
5. **Dark Mode**: Ensure components work in both themes
6. **Documentation**: Document custom components and patterns

## Contributing

When adding new components:

1. Follow existing patterns and conventions
2. Include TypeScript types
3. Add dark mode support
4. Ensure accessibility compliance
5. Include loading and error states
6. Add to this documentation
7. Test across different screen sizes
