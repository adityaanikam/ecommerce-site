import { test, expect } from '@playwright/test'

test.describe('LoginForm Visual Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })

  test('LoginForm should match visual snapshot', async ({ page }) => {
    const loginForm = page.locator('[data-testid="login-form"]')
    await expect(loginForm).toHaveScreenshot('login-form.png')
  })

  test('LoginForm with validation errors should match visual snapshot', async ({ page }) => {
    const submitButton = page.locator('button[type="submit"]')
    await submitButton.click()
    
    const loginForm = page.locator('[data-testid="login-form"]')
    await expect(loginForm).toHaveScreenshot('login-form-validation-errors.png')
  })

  test('LoginForm loading state should match visual snapshot', async ({ page }) => {
    const emailInput = page.locator('input[type="email"]')
    const passwordInput = page.locator('input[type="password"]')
    const submitButton = page.locator('button[type="submit"]')
    
    await emailInput.fill('test@example.com')
    await passwordInput.fill('password123')
    await submitButton.click()
    
    const loginForm = page.locator('[data-testid="login-form"]')
    await expect(loginForm).toHaveScreenshot('login-form-loading.png')
  })

  test('LoginForm password visibility toggle should match visual snapshot', async ({ page }) => {
    const passwordInput = page.locator('input[type="password"]')
    const toggleButton = page.locator('button[aria-label="Toggle password visibility"]')
    
    await toggleButton.click()
    
    const loginForm = page.locator('[data-testid="login-form"]')
    await expect(loginForm).toHaveScreenshot('login-form-password-visible.png')
  })
})
