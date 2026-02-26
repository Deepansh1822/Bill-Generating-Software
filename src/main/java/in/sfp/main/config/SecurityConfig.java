package in.sfp.main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                // Register JWT filter before UsernamePasswordAuthenticationFilter
                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                http
                                .csrf(csrf -> csrf.disable())

                                .authorizeHttpRequests(auth -> auth
                                                // ---- PUBLIC API endpoints (no login needed) ----
                                                .requestMatchers(
                                                                "/billing-app/api/login",
                                                                "/billing-app/api/logout",
                                                                "/billing-app/api/saveAccessRequests",
                                                                "/billing-app/api/forgot-password",
                                                                "/billing-app/api/setup-password",
                                                                "/billing-app/api/internal/**",
                                                                "/api-diag/**")
                                                .permitAll()

                                                // ---- Public pages: Login, MainDashboard, Dashboard and RequestAccess ----
                                                .requestMatchers(
                                                                "/billing-app/api/Login",
                                                                "/billing-app/api/MainDashboard",
                                                                "/billing-app/api/RequestAccess")
                                                .permitAll()

                                                // ---- Static resources ----
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**")
                                                .permitAll()

                                                // ---- Admin-only endpoints ----
                                                .requestMatchers(
                                                                "/billing-app/api/admin/**",
                                                                "/billing-app/api/getAllClients",
                                                                "/billing-app/api/ManageAccessRequests",
                                                                "/billing-app/api/ManageClients")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/billing-app/api/generateBill",
                                                                "/billing-app/api/GenerateBill")
                                                .hasRole("CLIENT")

                                                // ---- Error page ----
                                                .requestMatchers("/error").permitAll()

                                                // ---- Everything else requires authentication ----
                                                .anyRequest().authenticated())

                                // Stateless — no server-side sessions for auth
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.disable()));

                return http.build();
        }
}
