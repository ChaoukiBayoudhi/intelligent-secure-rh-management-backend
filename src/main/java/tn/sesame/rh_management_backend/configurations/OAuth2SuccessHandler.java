package tn.sesame.rh_management_backend.configurations;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tn.sesame.rh_management_backend.Entities.User;
import tn.sesame.rh_management_backend.Enumerations.UserRole;
import tn.sesame.rh_management_backend.Repositories.UserRepository;
import tn.sesame.rh_management_backend.configurations.JwtUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * OAuth2.1 Authentication Success Handler
 * 
 * This handler is called when a user successfully authenticates via OAuth2
 * (e.g., Google, GitHub login).
 * 
 * What happens here:
 * 1. User clicks "Login with Google" or "Login with GitHub"
 * 2. They are redirected to the OAuth2 provider (Google/GitHub)
 * 3. User authorizes the application
 * 4. OAuth2 provider redirects back to our application with an authorization code
 * 5. Spring Security exchanges the code for an access token
 * 6. Spring Security fetches user information from the provider
 * 7. This handler is called with the authenticated OAuth2 user
 * 8. We create or update the user in our database
 * 9. We generate our own JWT token for the user
 * 10. We redirect the user to the frontend with the token
 * 
 * Why generate our own JWT?
 * - OAuth2 tokens from providers are short-lived
 * - We want to maintain our own session management
 * - We can add our own claims (like user roles)
 * - We have full control over token expiration
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // Frontend URL - configurable via application.properties
    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // OAuth2 callback path - configurable via application.properties
    @Value("${frontend.oauth2.callback.path:/oauth2/callback}")
    private String callbackPath;

    // OAuth2 error path - configurable via application.properties
    @Value("${frontend.oauth2.error.path:/oauth2/error}")
    private String errorPath;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        
        try {
            log.info("OAuth2 authentication successful for user: {}", authentication.getName());
            
            // Get the OAuth2 user details
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // Extract user information from OAuth2 provider
            // Different providers return different attribute names
            String email = extractEmail(oauth2User);
            String name = extractName(oauth2User);
            String provider = extractProvider(oauth2User);
            
            // Validate email was extracted successfully
            if (email == null || email.isEmpty()) {
                log.error("Failed to extract email from OAuth2 user: {}", oauth2User.getAttributes());
                redirectToErrorPage(request, response, "Failed to extract user email from OAuth2 provider");
                return;
            }
            
            log.info("OAuth2 user details - Email: {}, Name: {}, Provider: {}", email, name, provider);
            
            // Check if user exists in our database
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                // User doesn't exist - create a new user
                // Default role for OAuth2 users is EMPLOYEE
                // You can change this logic based on your requirements
                log.info("Creating new user from OAuth2: {}", email);
                
                try {
                    user = User.builder()
                            .email(email)
                            .password(null) // OAuth2 users don't have passwords - null is allowed
                            .role(UserRole.EMPLOYEE) // Default role - you can customize this
                            .emailVerified(true) // OAuth2 providers verify emails
                            .accountLocked(false)
                            .failedLoginAttempts(0)
                            .mfaEnabled(false)
                            .oauth2Provider(provider) // Track which OAuth2 provider was used
                            .createdAt(Instant.now())
                            .lastLoginAt(Instant.now())
                            .build();
                    
                    user = userRepository.save(user);
                    log.info("New OAuth2 user created: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to create OAuth2 user: {}", e.getMessage(), e);
                    redirectToErrorPage(request, response, "Failed to create user account");
                    return;
                }
            } else {
                // User exists - update last login time and provider
                try {
                    user.setLastLoginAt(Instant.now());
                    // Update provider if not set (for existing users)
                    if (user.getOauth2Provider() == null || user.getOauth2Provider().isEmpty()) {
                        user.setOauth2Provider(provider);
                    }
                    userRepository.save(user);
                    log.info("Existing OAuth2 user logged in: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Failed to update OAuth2 user: {}", e.getMessage(), e);
                    redirectToErrorPage(request, response, "Failed to update user account");
                    return;
                }
            }
            
            // Generate our own JWT tokens for the user
            // This allows the user to use our API with standard JWT authentication
            String token;
            String refreshToken;
            try {
                token = jwtUtil.generateToken(user);
                refreshToken = jwtUtil.generateRefreshToken(user);
                log.info("JWT tokens generated for OAuth2 user: {}", email);
            } catch (Exception e) {
                log.error("Failed to generate JWT tokens: {}", e.getMessage(), e);
                redirectToErrorPage(request, response, "Failed to generate authentication tokens");
                return;
            }
            
            // Build the redirect URL with tokens
            // SECURITY: Using URL fragments (#) instead of query parameters
            // Fragments are not sent to the server, only processed by the browser
            // This prevents tokens from appearing in server logs or referrer headers
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + callbackPath)
                    .fragment("token=" + token + 
                             "&refreshToken=" + refreshToken + 
                             "&email=" + email + 
                             "&role=" + user.getRole().name())
                    .build()
                    .toUriString();
            
            log.info("Redirecting OAuth2 user to frontend (tokens in fragment)");
            
            // Redirect to frontend with tokens in URL fragment
            // Frontend JavaScript will extract tokens from the fragment
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("Unexpected error in OAuth2 success handler: {}", e.getMessage(), e);
            redirectToErrorPage(request, response, "An unexpected error occurred during authentication");
        }
    }
    
    /**
     * Redirect to error page with error message
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param errorMessage Error message to display
     */
    private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + errorPath)
                .queryParam("error", "oauth2_authentication_failed")
                .queryParam("message", errorMessage)
                .build()
                .toUriString();
        
        log.info("Redirecting to error page: {}", errorUrl);
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }

    /**
     * Extract email from OAuth2 user attributes
     * Different providers use different attribute names
     */
    private String extractEmail(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Google uses "email"
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        
        // GitHub uses "email" but might be null if email is not public
        // GitHub API requires "user:email" scope to get email
        // If email is not available, we cannot create a user account
        // The user must make their email public or grant email access
        if (attributes.containsKey("login") && !attributes.containsKey("email")) {
            log.warn("GitHub user email not available. User may need to grant email access or make email public.");
            // Return null to trigger error handling - we cannot proceed without email
            return null;
        }
        
        // Fallback to name attribute
        return oauth2User.getName();
    }

    /**
     * Extract name from OAuth2 user attributes
     */
    private String extractName(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Google uses "name"
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        }
        
        // GitHub uses "name" or "login"
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        }
        
        return oauth2User.getName();
    }

    /**
     * Extract provider name from OAuth2 user
     * This helps us identify which OAuth2 provider was used
     */
    private String extractProvider(OAuth2User oauth2User) {
        // This is a simplified version
        // In a real implementation, you might want to store the provider in the user entity
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        // Google has "sub" attribute with specific format
        if (attributes.containsKey("sub") && attributes.get("sub").toString().contains("google")) {
            return "google";
        }
        
        // GitHub has "login" attribute
        if (attributes.containsKey("login")) {
            return "github";
        }
        
        return "unknown";
    }
}
