package tn.sesame.rh_management_backend.configurations;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2.1 Authentication Failure Handler
 * 
 * This handler is called when OAuth2 authentication fails.
 * 
 * Common failure scenarios:
 * 1. User denies authorization on the OAuth2 provider
 * 2. Invalid OAuth2 client credentials
 * 3. Network issues during OAuth2 flow
 * 4. OAuth2 provider returns an error
 * 
 * This handler logs the error and redirects the user to the frontend
 * with an error message so they can try again or use a different login method.
 */
@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // Frontend URL where we'll redirect after OAuth2 failure
    private static final String FRONTEND_URL = "http://localhost:3000";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        
        // Log the authentication failure for debugging
        log.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        
        // Get the error message
        String errorMessage = exception.getMessage();
        
        // Build the redirect URL with error information
        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_URL + "/oauth2/error")
                .queryParam("error", "oauth2_authentication_failed")
                .queryParam("message", errorMessage != null ? errorMessage : "Authentication failed")
                .build()
                .toUriString();
        
        log.info("Redirecting OAuth2 failure to: {}", targetUrl);
        
        // Redirect to frontend with error
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
