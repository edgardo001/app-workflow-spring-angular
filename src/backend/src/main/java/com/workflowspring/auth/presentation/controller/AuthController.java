package com.workflowspring.auth.presentation.controller;

import com.workflowspring.auth.AuthResponse;
import com.workflowspring.auth.AuthService;
import com.workflowspring.auth.JwtTokenService;
import com.workflowspring.auth.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @Value("${github.client-id}")
    private String githubClientId;

    @Value("${github.redirect-uri}")
    private String githubRedirectUri;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/github")
    public ResponseEntity<Void> githubLogin() {
        String url = "https://github.com/login/oauth/authorize?client_id=" + githubClientId.trim() + "&scope=user:email&redirect_uri=" + githubRedirectUri.trim();
        return ResponseEntity.status(302).header("Location", url).build();
    }

    @GetMapping("/github/callback")
    public ResponseEntity<Void> githubCallbackGet(@org.springframework.web.bind.annotation.RequestParam("code") String code, jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        AuthResponse response = authService.processOAuthLogin(code);

        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("auth_token", response.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(302).header("Location", frontendUrl.trim() + "/dashboard").build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Void> handleOAuthError(RuntimeException ex, jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        log.error("OAuth error during callback: {}", ex.getMessage());
        return ResponseEntity.status(302)
                .header("Location", frontendUrl.trim() + "/login?error=" + ex.getMessage())
                .build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> profile() {
        UserProfile user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Boolean>> verifyToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        boolean valid = jwtTokenService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        // Clear host-only cookie
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        // Clear localhost domain cookie
        org.springframework.http.ResponseCookie cookieLocal = org.springframework.http.ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .domain("localhost")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookieLocal.toString());

        // Clear 127.0.0.1 domain cookie
        org.springframework.http.ResponseCookie cookieIp = org.springframework.http.ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .domain("127.0.0.1")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookieIp.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
