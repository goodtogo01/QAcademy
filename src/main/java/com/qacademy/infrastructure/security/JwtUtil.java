package com.qacademy.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Equivalent of qEducation/qCampus's AuthService JWT-issuing logic - claims are
// the payload of the token, and this is what a role check downstream verifies
// against on every request without hitting the database again.
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String PLACEHOLDER_SECRET =
            "REPLACE_THIS_WITH_A_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS_LONG";

    private final SecretKey key;
    private final String issuer;
    private final String audience;
    private final long expirationHours;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience,
            @Value("${jwt.expiration-hours}") long expirationHours) {
        if (PLACEHOLDER_SECRET.equals(secret)) {
            // Not fail-fast: this placeholder is the documented, intended value for a
            // local/demo run with no JWT_SECRET env var set. But since every token this
            // process issues is forgeable by anyone who read this committed, public
            // source file, it must never be reached in anything but a local/demo run.
            log.warn("=================================================================");
            log.warn("SECURITY WARNING: jwt.secret is still the placeholder value that is");
            log.warn("committed in application.properties. Any JWT this server issues can");
            log.warn("be forged by anyone who has read the public source code. Set a real");
            log.warn("JWT_SECRET environment variable for any deployment that isn't your");
            log.warn("own local machine.");
            log.warn("=================================================================");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.expirationHours = expirationHours;
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationHours * 3600_000L);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }
}
