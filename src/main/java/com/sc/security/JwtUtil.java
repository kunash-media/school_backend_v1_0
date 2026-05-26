package com.sc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        logger.trace("Signing key initialized (algorithm: {})", key.getAlgorithm());
        return key;
    }

    /**
     * Extracts the username (subject) from the JWT token.
     */
    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Extracted subject (username) from token: {}", username);
            return username;
        } catch (Exception e) {
            logger.warn("Failed to extract subject from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts the expiration date from the JWT token.
     */
    public Date extractExpiration(String token) {
        try {
            Date exp = extractClaim(token, Claims::getExpiration);
            logger.debug("Extracted expiration date: {}", exp);
            return exp;
        } catch (Exception e) {
            logger.warn("Failed to extract expiration from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Generic method to extract any claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and verifies all claims from the token.
     * Throws specific JWT exceptions with appropriate logging.
     */
    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            logger.trace("Token claims successfully parsed. Subject: {}, Issued: {}, Expires: {}",
                    claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());

            return claims;

        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired at {}: {}", e.getClaims().getExpiration(), e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error parsing JWT token", e);
            throw e;
        }
    }

    /**
     * Checks if the token has expired.
     */
    private boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        if (expired) {
            logger.debug("Token is expired");
        } else {
            logger.trace("Token is still valid");
        }
        return expired;
    }

    /**
     * Generates a new JWT token for the given user.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Optional: include user roles/authorities in the token
        // Uncomment when you want roles available in the token payload
        /*
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        */

        String username = userDetails.getUsername();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        logger.info("Generated JWT for user: {} | expires in {} ms (at {})",
                username, expiration, expiryDate);

        // Safe logging of token prefix (never log full token!)
        String tokenPreview = token.length() > 20 ? token.substring(0, 20) + "..." : token;
        logger.debug("Generated token preview: {}", tokenPreview);

        return token;
    }

    /**
     * Validates if the token is valid and belongs to the given user.
     * Returns true only if valid and not expired.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);

            if (!username.equals(userDetails.getUsername())) {
                logger.warn("Token subject mismatch. Expected: {}, Found in token: {}",
                        userDetails.getUsername(), username);
                return false;
            }

            if (isTokenExpired(token)) {
                logger.warn("Token expired for user: {}", username);
                return false;
            }

            logger.info("JWT token successfully validated for user: {}", username);
            return true;

        } catch (ExpiredJwtException e) {
            logger.warn("Token validation failed - expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }
}