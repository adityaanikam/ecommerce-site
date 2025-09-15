import { test, expect } from '@playwright/test'

test.describe('ProductCard Visual Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products')
  })

  test('ProductCard should match visual snapshot', async ({ page }) => {
    const productCard = page.locator('[data-testid="product-card"]').first()
    await expect(productCard).toHaveScreenshot('product-card.png')
  })

  test('ProductCard with discount should match visual snapshot', async ({ page }) => {
    const productCard = page.locator('[data-testid="product-card"]').first()
    await expect(productCard).toHaveScreenshot('product-card-discount.png')
  })

  test('ProductCard out of stock should match visual snapshot', async ({ page }) => {
    const productCard = page.locator('[data-testid="product-card"]').first()
    await expect(productCard).toHaveScreenshot('product-card-out-of-stock.png')
  })

  test('ProductCard hover state should match visual snapshot', async ({ page }) => {
    const productCard = page.locator('[data-testid="product-card"]').first()
    await productCard.hover()
    await expect(productCard).toHaveScreenshot('product-card-hover.png')
  })
})
