package tn.sesame.rh_management_backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * OAuth2.1 Resource Server Configuration
 * 
 * This configuration allows your application to validate OAuth2 JWT tokens.
 * 
 * What is OAuth2.1?
 * - OAuth2.1 is the latest version of OAuth2 that simplifies and improves security
 * - It removes deprecated features like implicit grant and password grant
 * - It enforces PKCE (Proof Key for Code Exchange) for better security
 * - It requires HTTPS for production environments
 * 
 * How it works:
 * 1. When a client sends a request with a JWT token in the Authorization header
 * 2. This configuration validates the token against the OAuth2 authorization server
 * 3. If valid, it extracts user information and authorities from the token
 * 4. The user is then authenticated and can access protected resources
 * 
 * In this project, we're using a hybrid approach:
 * - We still support our custom JWT tokens (via JwtUtil)
 * - We also support OAuth2.1 JWT tokens from external providers
 * - This gives flexibility to use either authentication method
 */
@Configuration
public class OAuth2ResourceServerConfig {

    // OAuth2 Resource Server issuer URI
    // This is the base URL of the OAuth2 authorization server
    // For external providers (Auth0, Okta), set this to their issuer URI
    // For our own tokens, this should match the application base URL
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:9995}")
    private String issuerUri;

    /**
     * JWT Decoder Bean
     * 
     * This bean is responsible for decoding and validating JWT tokens.
     * It connects to the OAuth2 authorization server to verify token signatures.
     * 
     * The issuer-uri is configured in application.properties:
     * spring.security.oauth2.resourceserver.jwt.issuer-uri
     * 
     * For external providers (like Auth0, Okta), you would set this to their issuer URI.
     * For our own application, this should match the server base URL.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use the issuer URI from application.properties
        // This allows configuration without code changes
        // Example for Auth0: https://your-domain.auth0.com/
        // Example for Okta: https://your-domain.okta.com/oauth2/default
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    /**
     * JWT Authentication Converter
     * 
     * This converter extracts authorities (roles) from the JWT token.
     * 
     * JWT tokens typically contain claims like:
     * - "sub" (subject): the user identifier
     * - "scope" or "authorities": the user's roles/permissions
     * - "email": the user's email
     * 
     * This converter maps those claims to Spring Security authorities.
     * 
     * In our case, we're looking for a "role" claim in the token
     * and converting it to a Spring Security authority (ROLE_ADMIN, ROLE_USER, etc.)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Create a converter for authorities
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Set the claim name where authorities are stored in the JWT
        // Common names: "scope", "authorities", "roles", "permissions"
        // In our custom JWT, we use "role"
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("role");
        
        // Create the authentication converter
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        // Set the principal claim name (usually "sub" for subject/user identifier)
        // In our case, we use "sub" which contains the email
        authenticationConverter.setPrincipalClaimName("sub");
        
        return authenticationConverter;
    }
}
