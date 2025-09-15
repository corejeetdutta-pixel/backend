package com.recruitment.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private String expirationString;

    private Long expiration;
    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.expiration = parseExpiration(expirationString);
        this.signingKey = getSigningKey();
    }

    private Long parseExpiration(String expirationValue) {
        try {
            if (expirationValue != null && expirationValue.contains("#")) {
                expirationValue = expirationValue.split("#")[0].trim();
            }
            return Long.valueOf(expirationValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expiration format: " + expirationValue, e);
        }
    }

    private SecretKey getSigningKey() {
        try {
            // Ensure the secret is at least 32 characters
            if (secret == null || secret.length() < 32) {
                throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
            }
            
            // Use the secret to create a proper signing key
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            
            // If the secret is exactly 32 characters, use it directly
            if (keyBytes.length == 32) {
                return Keys.hmacShaKeyFor(keyBytes);
            }
            
            // If the secret is longer than 32 characters, truncate it
            if (keyBytes.length > 32) {
                byte[] truncatedKey = new byte[32];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
                return Keys.hmacShaKeyFor(truncatedKey);
            }
            
            // If the secret is shorter than 32 characters, pad it with zeros
            // Note: This is not recommended for production - use a proper 32-character secret
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            return Keys.hmacShaKeyFor(paddedKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWT signing key: " + e.getMessage(), e);
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract username from token: " + e.getMessage(), e);
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract expiration from token: " + e.getMessage(), e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract claim from token: " + e.getMessage(), e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("JWT token has expired", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token format", e);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new RuntimeException("JWT token is not supported", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token: " + e.getMessage(), e);
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check token expiration: " + e.getMessage(), e);
        }
    }

    public String generateToken(UserDetails userDetails, String userId, String role) {
        try {
            System.out.println("generate token is triggered...");
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("role", role);
            return createToken(claims, userDetails.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            System.out.println("create token is triggered...");
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            System.out.println("token kya generate hua.. " + token);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWT token: " + e.getMessage(), e);
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    // Additional utility methods
    
    public String getUserIdFromToken(String token) {
        try {
            return extractClaim(token, claims -> claims.get("userId", String.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user ID from token: " + e.getMessage(), e);
        }
    }
    
    public String getRoleFromToken(String token) {
        try {
            return extractClaim(token, claims -> claims.get("role", String.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract role from token: " + e.getMessage(), e);
        }
    }
    
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Getter for the parsed expiration (optional)
    public Long getExpiration() {
        return expiration;
    }
}