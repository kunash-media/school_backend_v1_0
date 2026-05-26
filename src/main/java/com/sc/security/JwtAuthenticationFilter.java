package com.sc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        logger.trace("JwtAuthenticationFilter processing request: {} {}", request.getMethod(), requestURI);

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Check for Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.debug("Bearer token found in header (length: {})", jwt.length());

            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to extract username from token on path {}: {}", requestURI, e.getMessage());
            }
        } else {
            logger.trace("No Bearer token in Authorization header for path: {}", requestURI);
        }

        // If we have a username and no existing authentication → try to authenticate
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Attempting to authenticate user: {} for path: {}", username, requestURI);

            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.trace("UserDetails loaded successfully for: {}", username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.info("Successfully authenticated user: {} via JWT for path: {}",
                            username, requestURI);
                } else {
                    logger.warn("JWT token validation failed for user: {} on path: {}",
                            username, requestURI);
                }

            } catch (Exception e) {
                logger.warn("Authentication failed for user {} on path {}: {}",
                        username, requestURI, e.getMessage(), e);
            }
        } else if (username == null) {
            logger.trace("No username extracted → skipping authentication for path: {}", requestURI);
        } else {
            logger.trace("Authentication already exists in SecurityContext → skipping for path: {}", requestURI);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}