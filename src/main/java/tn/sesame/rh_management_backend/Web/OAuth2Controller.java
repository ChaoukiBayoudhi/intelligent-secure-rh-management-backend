package tn.sesame.rh_management_backend.Web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2.1 Controller
 * 
 * This controller provides endpoints for OAuth2 authentication.
 * 
 * OAuth2 Login Flow:
 * 1. User clicks "Login with Google" or "Login with GitHub"
 * 2. Frontend redirects to: GET /api/oauth2/authorize/google or /api/oauth2/authorize/github
 * 3. Spring Security automatically redirects to the OAuth2 provider
 * 4. User authorizes the application on the provider's website
 * 5. Provider redirects back to: /login/oauth2/code/{provider}
 * 6. OAuth2SuccessHandler processes the authentication and generates JWT tokens
 * 7. User is redirected to frontend with tokens
 * 
 * Available OAuth2 Providers:
 * - Google: /oauth2/authorization/google
 * - GitHub: /oauth2/authorization/github
 * 
 * Note: Spring Security automatically creates these endpoints.
 * This controller provides additional information and helper endpoints.
 */
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    /**
     * Get available OAuth2 providers
     * 
     * This endpoint returns a list of OAuth2 providers that are configured
     * and available for login.
     * 
     * Frontend can use this to display login buttons for each provider.
     * 
     * @return Map containing OAuth2 provider information
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        Map<String, Object> providers = new HashMap<>();
        
        // Google OAuth2 provider
        Map<String, String> google = new HashMap<>();
        google.put("name", "Google");
        google.put("authorizationUrl", "/oauth2/authorization/google");
        google.put("icon", "google"); // Frontend can use this to display Google icon
        
        // GitHub OAuth2 provider
        Map<String, String> github = new HashMap<>();
        github.put("name", "GitHub");
        github.put("authorizationUrl", "/oauth2/authorization/github");
        github.put("icon", "github"); // Frontend can use this to display GitHub icon
        
        providers.put("google", google);
        providers.put("github", github);
        
        return ResponseEntity.ok(providers);
    }

    /**
     * OAuth2 login information endpoint
     * 
     * This endpoint provides information about OAuth2 login flow
     * and how to use it from the frontend.
     * 
     * @return Map containing OAuth2 login information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getOAuth2Info() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("message", "OAuth2.1 authentication is enabled");
        info.put("description", "Users can login using Google or GitHub accounts");
        info.put("flow", "Authorization Code Flow with PKCE (OAuth2.1 standard)");
        info.put("endpoints", Map.of(
                "google", "/oauth2/authorization/google",
                "github", "/oauth2/authorization/github",
                "callback", "/login/oauth2/code/{provider}"
        ));
        info.put("note", "After successful OAuth2 login, users receive JWT tokens for API access");
        
        return ResponseEntity.ok(info);
    }
}
