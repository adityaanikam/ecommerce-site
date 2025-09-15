interface TokenData {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
  user: {
    id: string;
    email: string;
    roles: string[];
  };
}

interface RememberMeData {
  email: string;
  rememberMe: boolean;
  expiresAt: number;
}

class TokenManager {
  private static readonly ACCESS_TOKEN_KEY = 'accessToken';
  private static readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private static readonly TOKEN_EXPIRES_KEY = 'tokenExpiresAt';
  private static readonly USER_DATA_KEY = 'userData';
  private static readonly REMEMBER_ME_KEY = 'rememberMeData';
  private static readonly SESSION_TIMEOUT_KEY = 'sessionTimeout';

  // Token storage with encryption (basic obfuscation)
  private static encrypt(data: string): string {
    return btoa(encodeURIComponent(data));
  }

  private static decrypt(encryptedData: string): string {
    try {
      return decodeURIComponent(atob(encryptedData));
    } catch {
      return '';
    }
  }

  // Store tokens
  static setTokens(tokenData: TokenData): void {
    try {
      localStorage.setItem(this.ACCESS_TOKEN_KEY, this.encrypt(tokenData.accessToken));
      localStorage.setItem(this.REFRESH_TOKEN_KEY, this.encrypt(tokenData.refreshToken));
      localStorage.setItem(this.TOKEN_EXPIRES_KEY, tokenData.expiresAt.toString());
      localStorage.setItem(this.USER_DATA_KEY, this.encrypt(JSON.stringify(tokenData.user)));
      
      // Set session timeout (15 minutes of inactivity)
      this.setSessionTimeout();
    } catch (error) {
      console.error('Failed to store tokens:', error);
    }
  }

  // Get access token
  static getAccessToken(): string | null {
    try {
      const encrypted = localStorage.getItem(this.ACCESS_TOKEN_KEY);
      return encrypted ? this.decrypt(encrypted) : null;
    } catch {
      return null;
    }
  }

  // Get refresh token
  static getRefreshToken(): string | null {
    try {
      const encrypted = localStorage.getItem(this.REFRESH_TOKEN_KEY);
      return encrypted ? this.decrypt(encrypted) : null;
    } catch {
      return null;
    }
  }

  // Get user data
  static getUserData(): TokenData['user'] | null {
    try {
      const encrypted = localStorage.getItem(this.USER_DATA_KEY);
      if (!encrypted) return null;
      
      const decrypted = this.decrypt(encrypted);
      return JSON.parse(decrypted);
    } catch {
      return null;
    }
  }

  // Check if token is expired
  static isTokenExpired(): boolean {
    const expiresAt = localStorage.getItem(this.TOKEN_EXPIRES_KEY);
    if (!expiresAt) return true;
    
    return Date.now() >= parseInt(expiresAt);
  }

  // Check if token needs refresh (refresh 5 minutes before expiry)
  static needsRefresh(): boolean {
    const expiresAt = localStorage.getItem(this.TOKEN_EXPIRES_KEY);
    if (!expiresAt) return true;
    
    const refreshThreshold = 5 * 60 * 1000; // 5 minutes
    return Date.now() >= (parseInt(expiresAt) - refreshThreshold);
  }

  // Clear all tokens
  static clearTokens(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRES_KEY);
    localStorage.removeItem(this.USER_DATA_KEY);
    localStorage.removeItem(this.SESSION_TIMEOUT_KEY);
  }

  // Remember me functionality
  static setRememberMe(email: string, rememberMe: boolean): void {
    if (rememberMe) {
      const rememberMeData: RememberMeData = {
        email,
        rememberMe: true,
        expiresAt: Date.now() + (30 * 24 * 60 * 60 * 1000), // 30 days
      };
      localStorage.setItem(this.REMEMBER_ME_KEY, this.encrypt(JSON.stringify(rememberMeData)));
    } else {
      localStorage.removeItem(this.REMEMBER_ME_KEY);
    }
  }

  static getRememberMeData(): RememberMeData | null {
    try {
      const encrypted = localStorage.getItem(this.REMEMBER_ME_KEY);
      if (!encrypted) return null;
      
      const decrypted = this.decrypt(encrypted);
      const data: RememberMeData = JSON.parse(decrypted);
      
      // Check if remember me data is expired
      if (Date.now() > data.expiresAt) {
        localStorage.removeItem(this.REMEMBER_ME_KEY);
        return null;
      }
      
      return data;
    } catch {
      return null;
    }
  }

  // Session timeout management
  static setSessionTimeout(): void {
    const timeout = Date.now() + (15 * 60 * 1000); // 15 minutes
    localStorage.setItem(this.SESSION_TIMEOUT_KEY, timeout.toString());
  }

  static isSessionExpired(): boolean {
    const timeout = localStorage.getItem(this.SESSION_TIMEOUT_KEY);
    if (!timeout) return true;
    
    return Date.now() > parseInt(timeout);
  }

  static updateSessionTimeout(): void {
    this.setSessionTimeout();
  }

  // Check if user is authenticated
  static isAuthenticated(): boolean {
    const hasToken = !!this.getAccessToken();
    const hasRefreshToken = !!this.getRefreshToken();
    const tokenNotExpired = !this.isTokenExpired();
    const sessionNotExpired = !this.isSessionExpired();
    
    return hasToken && hasRefreshToken && tokenNotExpired && sessionNotExpired;
  }

  // Check if user has specific role
  static hasRole(role: string): boolean {
    const userData = this.getUserData();
    return userData?.roles.includes(role) || false;
  }

  // Check if user has any of the specified roles
  static hasAnyRole(roles: string[]): boolean {
    const userData = this.getUserData();
    if (!userData) return false;
    
    return roles.some(role => userData.roles.includes(role));
  }

  // Get token expiration time
  static getTokenExpirationTime(): number | null {
    const expiresAt = localStorage.getItem(this.TOKEN_EXPIRES_KEY);
    return expiresAt ? parseInt(expiresAt) : null;
  }

  // Get time until token expires (in milliseconds)
  static getTimeUntilExpiry(): number {
    const expiresAt = this.getTokenExpirationTime();
    if (!expiresAt) return 0;
    
    return Math.max(0, expiresAt - Date.now());
  }

  // Format time until expiry for display
  static getFormattedTimeUntilExpiry(): string {
    const timeUntilExpiry = this.getTimeUntilExpiry();
    if (timeUntilExpiry === 0) return 'Expired';
    
    const minutes = Math.floor(timeUntilExpiry / (1000 * 60));
    const seconds = Math.floor((timeUntilExpiry % (1000 * 60)) / 1000);
    
    if (minutes > 0) {
      return `${minutes}m ${seconds}s`;
    }
    
    return `${seconds}s`;
  }

  // Auto-refresh token if needed
  static async autoRefreshToken(): Promise<boolean> {
    if (!this.needsRefresh()) return true;
    
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return false;
    
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });
      
      if (!response.ok) {
        this.clearTokens();
        return false;
      }
      
      const data = await response.json();
      const newTokenData: TokenData = {
        accessToken: data.data.accessToken,
        refreshToken: data.data.refreshToken,
        expiresAt: Date.now() + (data.data.expiresIn * 1000),
        user: data.data.user,
      };
      
      this.setTokens(newTokenData);
      return true;
    } catch (error) {
      console.error('Token refresh failed:', error);
      this.clearTokens();
      return false;
    }
  }

  // Initialize token manager (call on app start)
  static async initialize(): Promise<boolean> {
    // Check if session is expired
    if (this.isSessionExpired()) {
      this.clearTokens();
      return false;
    }
    
    // Auto-refresh token if needed
    return await this.autoRefreshToken();
  }

  // Setup automatic token refresh
  static setupAutoRefresh(): void {
    // Refresh token every 4 minutes
    setInterval(async () => {
      if (this.isAuthenticated()) {
        await this.autoRefreshToken();
      }
    }, 4 * 60 * 1000);
  }

  // Setup session timeout warning
  static setupSessionTimeoutWarning(
    onWarning: (timeLeft: number) => void,
    onExpired: () => void,
    warningTime: number = 2 * 60 * 1000 // 2 minutes before expiry
  ): void {
    setInterval(() => {
      if (!this.isAuthenticated()) return;
      
      const timeUntilExpiry = this.getTimeUntilExpiry();
      
      if (timeUntilExpiry <= 0) {
        onExpired();
      } else if (timeUntilExpiry <= warningTime) {
        onWarning(timeUntilExpiry);
      }
    }, 1000); // Check every second
  }
}

export default TokenManager;
