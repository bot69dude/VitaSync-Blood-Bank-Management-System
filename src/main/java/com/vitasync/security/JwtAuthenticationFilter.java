package com.vitasync.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    username = jwtTokenUtil.extractUsername(token);
                } catch (io.jsonwebtoken.JwtException ex) {
                    // malformed or invalid token - don't abort the request, just skip auth
                    logger.warn("Invalid JWT token received: {}", ex.getMessage());
                    token = null;
                } catch (Exception ex) {
                    logger.warn("Error while parsing JWT token: {}", ex.getMessage());
                    token = null;
                }
            } else {
                logger.debug("Authorization header contained empty Bearer token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Extract role from JWT token instead of querying database
            String role = null;
            if (token != null) {
                try {
                    role = jwtTokenUtil.extractRole(token);
                } catch (io.jsonwebtoken.JwtException ex) {
                    logger.warn("Invalid JWT when extracting role: {}", ex.getMessage());
                    role = null;
                } catch (Exception ex) {
                    logger.warn("Error extracting role from JWT: {}", ex.getMessage());
                    role = null;
                }
            }

            if (role != null) {
                // Create UserDetails from JWT token data
                UserDetails userDetails = User.withUsername(username)
                    .password("") // Password not needed for token validation
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                    .build();

                // Validate token with extracted user details
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                // Fallback to database lookup for tokens without role (backward compatibility)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
