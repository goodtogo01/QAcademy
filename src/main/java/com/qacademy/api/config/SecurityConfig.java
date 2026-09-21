package com.qacademy.api.config;

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
// valid JWT plus role checks enforced with @PreAuthorize on each controller method.
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtUtil jwtUtil, CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.authenticationEntryPoint = authenticationEntryPoint;
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
                .anyRequest().authenticated()
            )
            // Without this, an unauthenticated request (missing/invalid Authorization
            // header) gets Spring Security's default bare 401 with no body - see
            // CustomAuthenticationEntryPoint for the response body this adds.
            .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint))
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
