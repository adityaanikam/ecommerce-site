# Custom Radio Buttons Integration Guide

## Overview
This guide shows you how to integrate custom-styled radio buttons that work perfectly on dark backgrounds.

## Files Created
- `frontend/src/styles/custom-radio.css` - Pure CSS implementation
- `frontend/src/styles/tailwind-radio.css` - Tailwind CSS implementation
- `frontend/src/components/CustomRadioButton.tsx` - React component (CSS version)
- `frontend/src/components/TailwindRadioButton.tsx` - React component (Tailwind version)
- `frontend/src/examples/radio-button-example.html` - Standalone HTML example

## Quick Start

### Option 1: Using Tailwind CSS (Recommended)

1. **Import the Tailwind styles** in your main CSS file:
```css
@import './styles/tailwind-radio.css';
```

2. **Use the TailwindRadioButton component**:
```tsx
import { TailwindRadioButton } from '@/components/TailwindRadioButton';

// In your component
<TailwindRadioButton
  id="electronics"
  name="category"
  value="electronics"
  checked={selectedCategory === 'electronics'}
  onChange={setSelectedCategory}
  label="Electronics"
  count={5}
/>
```

### Option 2: Using Pure CSS

1. **Import the CSS file**:
```css
@import './styles/custom-radio.css';
```

2. **Use the CustomRadioButton component**:
```tsx
import { CustomRadioButton } from '@/components/CustomRadioButton';

// In your component
<CustomRadioButton
  id="electronics"
  name="category"
  value="electronics"
  checked={selectedCategory === 'electronics'}
  onChange={setSelectedCategory}
  label="Electronics"
/>
```

### Option 3: Direct HTML Implementation

```html
<div class="custom-radio">
  <input type="radio" id="electronics" name="category" value="electronics" checked>
  <label for="electronics" class="custom-radio-label">Electronics</label>
</div>
```

## Features

### ✅ Dark Theme Support
- Automatically adapts to dark backgrounds
- Proper contrast ratios
- Smooth transitions

### ✅ Accessibility
- Keyboard navigation support
- Screen reader friendly
- Focus indicators

### ✅ Customization Options
- **Sizes**: `sm`, `md`, `lg`
- **Colors**: `primary`, `success`, `warning`
- **States**: hover, focus, checked, disabled

### ✅ Animation
- Smooth transitions
- Hover effects
- Focus rings

## Styling Details

### Unselected State
- Grey border circle
- Transparent background
- Hover effects

### Selected State
- Blue border circle
- Solid blue inner circle
- Smooth animation

### Dark Theme
- Lighter grey borders for better visibility
- Adjusted blue colors for dark backgrounds
- Proper contrast ratios

## Integration with Existing Components

To integrate with your existing SearchFilters component:

```tsx
// Replace the existing radio inputs with:
<TailwindRadioButton
  id={`category-${cat.slug}`}
  name="category"
  value={cat.slug}
  checked={category === cat.slug}
  onChange={onCategoryChange}
  label={cat.name}
  count={cat.subcategories.reduce((acc, sub) => acc + sub.count, 0)}
/>
```

## Browser Support
- Modern browsers (Chrome, Firefox, Safari, Edge)
- IE11+ (with some limitations)
- Mobile browsers

## Performance
- Lightweight CSS (minimal impact)
- No JavaScript dependencies
- Hardware-accelerated animations
