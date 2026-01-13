package tn.sesame.rh_management_backend.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Main Security Configuration
 * 
 * This class configures Spring Security for the application.
 * 
 * Authentication Methods Supported:
 * 1. Custom JWT Authentication (via JwtAuthenticationFilter)
 *    - Users login with email/password
 *    - Receive JWT token
 *    - Use token in Authorization header for subsequent requests
 * 
 * 2. OAuth2.1 Resource Server
 *    - Validates OAuth2 JWT tokens from external providers
 *    - Can accept tokens from Auth0, Okta, or other OAuth2 providers
 * 
 * 3. OAuth2.1 Client (Google, GitHub login)
 *    - Users can login using their Google or GitHub accounts
 *    - After OAuth2 login, users receive our own JWT tokens
 *    - This provides a seamless login experience
 * 
 * Security Flow:
 * - Public endpoints: /api/auth/**, /h2-console/**, /actuator/**
 * - OAuth2 login endpoints: /oauth2/authorization/**, /login/oauth2/code/**
 * - Protected endpoints: All other endpoints require authentication
 * - Role-based access: Different roles have access to different endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Main Security Filter Chain
     * 
     * This is the heart of Spring Security configuration.
     * It defines:
     * - Which endpoints are public vs protected
     * - How authentication works (JWT, OAuth2)
     * - How authorization works (role-based access)
     * - CORS configuration
     * - Session management
     * 
     * Authentication Priority:
     * 1. First, try custom JWT authentication (JwtAuthenticationFilter)
     * 2. If that fails, try OAuth2 Resource Server authentication
     * 3. For OAuth2 client login, use OAuth2 login endpoints
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API (we use JWT tokens instead)
                // In production with cookies, you might want to enable CSRF
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configure CORS (Cross-Origin Resource Sharing)
                // This allows your frontend to make requests to this API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configure authorization rules
                // Define which endpoints require authentication and which roles can access them
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll() // Login, register, etc.
                        .requestMatchers("/api/oauth2/**").permitAll() // OAuth2 info endpoints
                        .requestMatchers("/h2-console/**").permitAll() // H2 database console (dev only)
                        .requestMatchers("/actuator/**").permitAll() // Spring Boot Actuator endpoints
                        
                        // OAuth2 login endpoints - public (Spring Security handles OAuth2 flow)
                        .requestMatchers("/oauth2/authorization/**").permitAll() // Initiate OAuth2 login
                        .requestMatchers("/login/oauth2/code/**").permitAll() // OAuth2 callback
                        
                        // Protected endpoints - require authentication and specific roles
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Only admins
                        .requestMatchers("/api/hr/**").hasAnyRole("HR_MANAGER", "ADMIN") // HR managers and admins
                        .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "HR_MANAGER", "ADMIN") // Managers, HR, and admins
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR_MANAGER", "ADMIN") // All authenticated users
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Configure session management
                // STATELESS means we don't use HTTP sessions
                // Each request must include authentication (JWT token)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configure OAuth2 Client (for Google, GitHub login)
                // This enables the OAuth2 login flow
                .oauth2Login(oauth2 -> oauth2
                        // Custom success handler - creates/updates user and generates JWT tokens
                        .successHandler(oAuth2SuccessHandler)
                        // Custom failure handler - redirects to frontend with error
                        .failureHandler(oAuth2FailureHandler)
                        // OAuth2 login page (Spring Security provides default, or you can customize)
                        .loginPage("/oauth2/authorization/google") // Default OAuth2 login endpoint
                )
                
                // Configure OAuth2 Resource Server
                // This allows the application to validate OAuth2 JWT tokens
                // You can use this if you want to accept tokens from external OAuth2 providers
                /*.oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Use the JWT authentication converter to extract roles from OAuth2 tokens
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )*/
                
                // Configure custom authentication provider (for email/password login)
                .authenticationProvider(authenticationProvider())
                
                // Add our custom JWT authentication filter
                // This filter runs before Spring Security's default authentication
                // It extracts JWT tokens from requests and authenticates users
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                // Configure HTTP headers
                .headers(headers -> headers
                        // Allow H2 console to be embedded in iframe (dev only)
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    /**
     * CORS Configuration
     * 
     * Configures Cross-Origin Resource Sharing (CORS) to allow frontend applications
     * to make requests to this API.
     * 
     * Security Best Practices:
     * - Specify exact origins instead of using "*"
     * - Specify exact headers instead of using "*" (especially with credentials)
     * - Use credentials only when necessary
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins - specify exact frontend URLs
        // In production, replace with actual frontend domain
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allowed headers - specify exact headers instead of "*" for better security
        // Common headers needed for API requests
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",      // For JWT tokens
                "Content-Type",       // For request body
                "X-Requested-With",   // For AJAX requests
                "Accept",             // For content negotiation
                "Origin",             // For CORS
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // Exposed headers - headers that the browser can access in the response
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));
        
        // Allow credentials (cookies, authorization headers)
        // Only enable if you need to send cookies or custom headers
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
