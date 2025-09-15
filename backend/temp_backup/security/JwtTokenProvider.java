package com.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;
    
    @Value("${jwt.refresh-expiration}")
    private int jwtRefreshExpirationInMs;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public JwtTokenProvider(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        String token = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("authorities", userPrincipal.getAuthorities())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Store token in Redis
        storeTokenInRedis(token, userPrincipal.getUsername(), jwtExpirationInMs);
        
        return token;
    }
    
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtRefreshExpirationInMs);
        
        String refreshToken = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Store refresh token in Redis
        storeTokenInRedis(refreshToken, userPrincipal.getUsername() + ":refresh", jwtRefreshExpirationInMs);
        
        return refreshToken;
    }
    
    public String generateTokenFromUsername(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        
        // Store token in Redis
        storeTokenInRedis(token, username, jwtExpirationInMs);
        
        return token;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            
            // Check if token exists in Redis (not blacklisted)
            return isTokenInRedis(token);
            
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    public void blacklistToken(String token) {
        String username = getUsernameFromToken(token);
        String key = "blacklist:" + token;
        
        // Store in Redis with expiration time
        long expirationTime = getExpirationDateFromToken(token).getTime() - System.currentTimeMillis();
        if (expirationTime > 0) {
            redisTemplate.opsForValue().set(key, username, expirationTime, TimeUnit.MILLISECONDS);
        }
    }
    
    public void revokeAllUserTokens(String username) {
        // Remove all tokens for user from Redis
        redisTemplate.delete("token:" + username);
        redisTemplate.delete("token:" + username + ":refresh");
    }
    
    private void storeTokenInRedis(String token, String key, long expirationInMs) {
        String redisKey = "token:" + key;
        redisTemplate.opsForValue().set(redisKey, token, expirationInMs, TimeUnit.MILLISECONDS);
    }
    
    private boolean isTokenInRedis(String token) {
        String username = getUsernameFromToken(token);
        String redisKey = "token:" + username;
        String storedToken = (String) redisTemplate.opsForValue().get(redisKey);
        
        return token.equals(storedToken);
    }
    
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return redisTemplate.hasKey(key);
    }
    
    public String refreshToken(String refreshToken) {
        if (validateToken(refreshToken) && !isTokenBlacklisted(refreshToken)) {
            String username = getUsernameFromToken(refreshToken);
            return generateTokenFromUsername(username);
        }
        throw new RuntimeException("Invalid refresh token");
    }
}