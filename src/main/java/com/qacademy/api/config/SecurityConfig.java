package com.qacademy.api.config;

import com.qacademy.api.security.CustomAccessDeniedHandler;
import com.qacademy.api.security.CustomAuthenticationEntryPoint;
import com.qacademy.api.security.JwtAuthenticationFilter;
import com.qacademy.infrastructure.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// GETs on /api/student and /api/course are open for demo purposes (see
// qEducation/qCampus's [AllowAnonymous] on the same endpoints); writes require a
// valid JWT plus role checks.
//
// Role checks for writes are enforced TWICE, deliberately:
//   1. Here, via .authorizeHttpRequests() HttpMethod/path matchers below - this runs
//      inside Spring Security's own AuthorizationFilter, before the request ever
//      reaches DispatcherServlet/the controller.
//   2. Via @PreAuthorize on each controller method, as defense-in-depth.
//
// Why both: a role denial that's only caught by @PreAuthorize throws its
// AccessDeniedException from deep inside DispatcherServlet's handler-method
// invocation (the method-security AOP interceptor), rather than from this filter
// chain directly. In this project that was observed to come back as 401 (via
// CustomAuthenticationEntryPoint) instead of the correct 403 for a validly
// authenticated STAFF/STUDENT token that's simply missing the required role
// (e.g. STAFF calling DELETE /api/student/{id}, which is ADMIN-only) - see
// JwtAuthenticationFilter's Javadoc and the qa_Academy_java project's
// 06-Regression-Failures-Investigation.md for how this was diagnosed. Denying at
// the .authorizeHttpRequests() layer instead means AuthorizationFilter itself
// throws the AccessDeniedException, in the exact same filter-chain context where
// the plain .anyRequest().authenticated() check already correctly recognizes a
// real (non-anonymous) authentication - which is what reliably produces 403
// instead of 401 for a real-but-insufficiently-privileged principal.
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtUtil jwtUtil, CustomAuthenticationEntryPoint authenticationEntryPoint,
                           CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtUtil = jwtUtil;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/assets/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/student/**", "/api/course/**", "/api/enrollment/**").permitAll()
                // Admin-only writes.
                .requestMatchers(HttpMethod.DELETE, "/api/student/**", "/api/enrollment/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/course/**").hasRole("ADMIN")
                // Admin-or-Staff writes.
                .requestMatchers(HttpMethod.POST, "/api/student/**", "/api/enrollment/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/student/**", "/api/enrollment/**").hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
            // Without this, an unauthenticated request (missing/invalid Authorization
            // header) gets Spring Security's default bare 401 with no body - see
            // CustomAuthenticationEntryPoint for the response body this adds.
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            // H2 console renders inside a frame; Spring Security's default
            // X-Frame-Options: DENY blocks it from rendering in the browser.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Constructed directly (not injected as a bean) so it's wired only
            // into this filter chain, not auto-registered a second time as a
            // blanket servlet filter - see the comment on JwtAuthenticationFilter.
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
