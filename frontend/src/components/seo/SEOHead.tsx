import React from 'react';
import { Helmet } from 'react-helmet-async';

interface SEOHeadProps {
  title?: string;
  description?: string;
  keywords?: string[];
  image?: string;
  url?: string;
  type?: 'website' | 'article' | 'product';
  author?: string;
  publishedTime?: string;
  modifiedTime?: string;
  section?: string;
  tags?: string[];
  price?: {
    amount: number;
    currency: string;
  };
  availability?: 'in stock' | 'out of stock' | 'preorder';
  brand?: string;
  category?: string;
  rating?: {
    value: number;
    count: number;
  };
  canonical?: string;
  noindex?: boolean;
  nofollow?: boolean;
  locale?: string;
  alternateLocales?: Array<{
    hreflang: string;
    href: string;
  }>;
  structuredData?: Record<string, any>;
}

export const SEOHead: React.FC<SEOHeadProps> = ({
  title = 'E-commerce Store - Modern Shopping Experience',
  description = 'Discover amazing products at our modern e-commerce store. Fast shipping, secure payments, and excellent customer service.',
  keywords = ['ecommerce', 'shopping', 'online store', 'products'],
  image = '/og-image.jpg',
  url = typeof window !== 'undefined' ? window.location.href : '',
  type = 'website',
  author,
  publishedTime,
  modifiedTime,
  section,
  tags,
  price,
  availability,
  brand,
  category,
  rating,
  canonical,
  noindex = false,
  nofollow = false,
  locale = 'en_US',
  alternateLocales = [],
  structuredData,
}) => {
  const siteName = 'E-commerce Store';
  const fullTitle = title.includes(siteName) ? title : `${title} | ${siteName}`;
  const fullImageUrl = image.startsWith('http') ? image : `${window.location.origin}${image}`;
  const fullUrl = url.startsWith('http') ? url : `${window.location.origin}${url}`;

  // Generate structured data
  const generateStructuredData = () => {
    const baseStructuredData = {
      '@context': 'https://schema.org',
      '@type': type === 'product' ? 'Product' : type === 'article' ? 'Article' : 'WebSite',
      name: title,
      description,
      url: fullUrl,
      image: fullImageUrl,
      ...structuredData,
    };

    if (type === 'product') {
      return {
        ...baseStructuredData,
        '@type': 'Product',
        brand: brand ? { '@type': 'Brand', name: brand } : undefined,
        category,
        offers: price ? {
          '@type': 'Offer',
          price: price.amount,
          priceCurrency: price.currency,
          availability: availability ? `https://schema.org/${availability.replace(' ', '')}` : undefined,
        } : undefined,
        aggregateRating: rating ? {
          '@type': 'AggregateRating',
          ratingValue: rating.value,
          reviewCount: rating.count,
        } : undefined,
      };
    }

    if (type === 'article') {
      return {
        ...baseStructuredData,
        '@type': 'Article',
        author: author ? { '@type': 'Person', name: author } : undefined,
        datePublished: publishedTime,
        dateModified: modifiedTime,
        section,
        keywords: tags?.join(', '),
      };
    }

    return baseStructuredData;
  };

  const structuredDataJson = generateStructuredData();

  return (
    <Helmet>
      {/* Basic Meta Tags */}
      <title>{fullTitle}</title>
      <meta name="description" content={description} />
      <meta name="keywords" content={keywords.join(', ')} />
      <meta name="author" content={author || siteName} />
      <meta name="robots" content={`${noindex ? 'noindex' : 'index'}, ${nofollow ? 'nofollow' : 'follow'}`} />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <meta httpEquiv="Content-Type" content="text/html; charset=utf-8" />
      <meta httpEquiv="X-UA-Compatible" content="IE=edge" />

      {/* Open Graph Meta Tags */}
      <meta property="og:type" content={type} />
      <meta property="og:title" content={fullTitle} />
      <meta property="og:description" content={description} />
      <meta property="og:image" content={fullImageUrl} />
      <meta property="og:url" content={fullUrl} />
      <meta property="og:site_name" content={siteName} />
      <meta property="og:locale" content={locale} />

      {/* Product-specific Open Graph tags */}
      {type === 'product' && (
        <>
          {price && (
            <>
              <meta property="product:price:amount" content={price.amount.toString()} />
              <meta property="product:price:currency" content={price.currency} />
            </>
          )}
          {availability && <meta property="product:availability" content={availability} />}
          {brand && <meta property="product:brand" content={brand} />}
          {category && <meta property="product:category" content={category} />}
        </>
      )}

      {/* Article-specific Open Graph tags */}
      {type === 'article' && (
        <>
          {author && <meta property="article:author" content={author} />}
          {publishedTime && <meta property="article:published_time" content={publishedTime} />}
          {modifiedTime && <meta property="article:modified_time" content={modifiedTime} />}
          {section && <meta property="article:section" content={section} />}
          {tags?.map((tag, index) => (
            <meta key={index} property="article:tag" content={tag} />
          ))}
        </>
      )}

      {/* Twitter Card Meta Tags */}
      <meta name="twitter:card" content="summary_large_image" />
      <meta name="twitter:title" content={fullTitle} />
      <meta name="twitter:description" content={description} />
      <meta name="twitter:image" content={fullImageUrl} />
      <meta name="twitter:site" content="@ecommercestore" />
      <meta name="twitter:creator" content="@ecommercestore" />

      {/* Canonical URL */}
      {canonical && <link rel="canonical" href={canonical} />}

      {/* Alternate Language Links */}
      {alternateLocales.map((locale, index) => (
        <link key={index} rel="alternate" hrefLang={locale.hreflang} href={locale.href} />
      ))}

      {/* Structured Data */}
      <script type="application/ld+json">
        {JSON.stringify(structuredDataJson, null, 2)}
      </script>

      {/* Additional Meta Tags */}
      <meta name="theme-color" content="#3b82f6" />
      <meta name="msapplication-TileColor" content="#3b82f6" />
      <meta name="apple-mobile-web-app-capable" content="yes" />
      <meta name="apple-mobile-web-app-status-bar-style" content="default" />
      <meta name="apple-mobile-web-app-title" content={siteName} />

      {/* Preconnect to external domains */}
      <link rel="preconnect" href="https://fonts.googleapis.com" />
      <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
      <link rel="preconnect" href="https://api.example.com" />

      {/* DNS Prefetch */}
      <link rel="dns-prefetch" href="//www.google-analytics.com" />
      <link rel="dns-prefetch" href="//fonts.googleapis.com" />
    </Helmet>
  );
};

// SEO utility functions
export const seoUtils = {
  // Generate product SEO data
  generateProductSEO: (product: {
    name: string;
    description: string;
    price: number;
    currency: string;
    images: string[];
    category: string;
    brand?: string;
    rating?: number;
    reviewCount?: number;
    availability: string;
    sku?: string;
  }) => ({
    title: product.name,
    description: product.description,
    type: 'product' as const,
    image: product.images[0],
    price: {
      amount: product.price,
      currency: product.currency,
    },
    availability: product.availability,
    brand: product.brand,
    category: product.category,
    rating: product.rating && product.reviewCount ? {
      value: product.rating,
      count: product.reviewCount,
    } : undefined,
    keywords: [product.name, product.category, product.brand].filter(Boolean),
    structuredData: {
      sku: product.sku,
      image: product.images,
    },
  }),

  // Generate category SEO data
  generateCategorySEO: (category: {
    name: string;
    description: string;
    productCount: number;
  }) => ({
    title: `${category.name} Products`,
    description: `${category.description} Browse ${category.productCount} products in ${category.name} category.`,
    type: 'website' as const,
    keywords: [category.name, 'products', 'category'],
  }),

  // Generate article SEO data
  generateArticleSEO: (article: {
    title: string;
    content: string;
    author: string;
    publishedAt: string;
    updatedAt?: string;
    tags: string[];
    image?: string;
  }) => ({
    title: article.title,
    description: article.content.substring(0, 160),
    type: 'article' as const,
    author: article.author,
    publishedTime: article.publishedAt,
    modifiedTime: article.updatedAt,
    tags: article.tags,
    image: article.image,
    keywords: article.tags,
  }),

  // Generate breadcrumb structured data
  generateBreadcrumbStructuredData: (breadcrumbs: Array<{ name: string; url: string }>) => ({
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement: breadcrumbs.map((breadcrumb, index) => ({
      '@type': 'ListItem',
      position: index + 1,
      name: breadcrumb.name,
      item: breadcrumb.url,
    })),
  }),

  // Generate FAQ structured data
  generateFAQStructuredData: (faqs: Array<{ question: string; answer: string }>) => ({
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: faqs.map(faq => ({
      '@type': 'Question',
      name: faq.question,
      acceptedAnswer: {
        '@type': 'Answer',
        text: faq.answer,
      },
    })),
  }),
};

export default SEOHead;
