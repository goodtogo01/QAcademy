package com.qacademy.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

// Fired by Spring Security when a request is authenticated (a real, valid
// principal) but that principal lacks the role/authority the endpoint requires -
// this is the correct 403 Forbidden case, distinct from CustomAuthenticationEntryPoint's
// 401 Unauthorized (no/invalid token at all).
//
// TEMPORARY DIAGNOSTIC: logs the SecurityContext's Authentication at the moment
// this handler is invoked, so we can confirm (or rule out) whether it's ever
// being reached at all for role-denied-but-authenticated requests, and what
// Spring Security believes the current principal/authorities are when it is.
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.warn("[DIAGNOSTIC] CustomAccessDeniedHandler fired for {} {} - authentication={}, principal={}, authorities={}, authenticated={}",
                request.getMethod(), request.getRequestURI(),
                auth,
                auth != null ? auth.getPrincipal() : null,
                auth != null ? auth.getAuthorities() : null,
                auth != null ? auth.isAuthenticated() : null);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("message", "You do not have permission to perform this action")
        ));
    }
}
