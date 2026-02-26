package in.sfp.main.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import in.sfp.main.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Public paths that should NEVER be processed by this JWT filter.
     * These must match the permitAll() paths in SecurityConfig.
     */
    private static final String[] PUBLIC_PATHS = {
            "/billing-app/api/login",
            "/billing-app/api/Login",
            "/billing-app/api/MainDashboard",
            "/billing-app/api/RequestAccess",
            "/billing-app/api/saveAccessRequests",
            "/billing-app/api/forgot-password",
            "/billing-app/api/setup-password",
            "/billing-app/api/internal/**",
            "/billing-app/api/logout",
            "/api-diag/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/error"
    };

    /**
     * Skip this filter entirely for public URLs.
     * This is the KEY fix — without this, the filter blocks public pages
     * before Spring Security's permitAll() rules can take effect.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (pathMatcher.match(publicPath, path)) {
                return true; // skip filter for this request
            }
        }
        return false; // filter WILL run for all other (secured) paths
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // --------------------------------------------------------------
        // 1. Extract JWT from Authorization header or cookie
        // --------------------------------------------------------------
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // --------------------------------------------------------------
        // 2. No token → clear context and let Spring Security reject it
        // (Spring Security will return 401/403 for protected endpoints)
        // --------------------------------------------------------------
        if (jwt == null || jwt.isBlank()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // --------------------------------------------------------------
        // 3. Token present — try to validate it
        // --------------------------------------------------------------
        String username = null;
        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Invalid or expired token → clear context, let Spring Security handle
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // --------------------------------------------------------------
        // 4. Valid token — set up Spring Security authentication
        // --------------------------------------------------------------
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {
                String role = jwtUtil.extractRole(jwt);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                        authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // Token failed validation → clear
                SecurityContextHolder.clearContext();
            }
        }

        // --------------------------------------------------------------
        // 5. Continue down the filter chain
        // --------------------------------------------------------------
        filterChain.doFilter(request, response);
    }
}
