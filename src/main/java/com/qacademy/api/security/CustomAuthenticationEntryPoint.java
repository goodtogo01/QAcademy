package com.qacademy.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

// Fired by Spring Security whenever an unauthenticated request reaches a
// protected endpoint - covers both "no Authorization header at all" and "header
// present but the token was rejected" (JwtAuthenticationFilter swallows parse
// errors and just clears the SecurityContext, so both cases fall through to
// here as a plain unauthenticated request). Without this bean, Spring Security's
// default entry point returns a bare 401 with no body.
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("message", "Due to Auth token missing, this request is forbidden")
        ));
    }
}
