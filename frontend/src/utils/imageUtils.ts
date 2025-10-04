/**
 * Utility functions for handling image URLs
 */

const BACKEND_BASE_URL = 'http://localhost:8080';

/**
 * Converts a relative image URL to a full URL
 * @param imageUrl - The image URL (can be relative or absolute)
 * @returns The full image URL
 */
export const getFullImageUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl) {
    return 'https://placehold.co/800x800/6366f1/ffffff?text=No+Image';
  }
  
  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }
  
  return `${BACKEND_BASE_URL}${imageUrl}`;
};

/**
 * Gets a fallback image URL for when images fail to load
 * @param size - The size of the placeholder image (default: 800x800)
 * @returns The fallback image URL
 */
export const getFallbackImageUrl = (size: string = '800x800'): string => {
  return `https://placehold.co/${size}/6366f1/ffffff?text=Image+Not+Found`;
};

/**
 * Handles image load error by setting a fallback image
 * @param event - The error event
 * @param size - The size of the fallback image
 */
export const handleImageError = (event: React.SyntheticEvent<HTMLImageElement, Event>, size: string = '800x800') => {
  const target = event.currentTarget;
  target.src = getFallbackImageUrl(size);
};
