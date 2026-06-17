package com.workflownet.auth.presentation.controller;

import com.workflownet.auth.AuthResponse;
import com.workflownet.auth.AuthService;
import com.workflownet.auth.JwtTokenService;
import com.workflownet.auth.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @org.springframework.beans.factory.annotation.Value("${github.client-id}")
    private String githubClientId;

    @GetMapping("/github")
    public ResponseEntity<Void> githubLogin() {
        String url = "https://github.com/login/oauth/authorize?client_id=" + githubClientId + "&scope=user:email&redirect_uri=http://localhost:4200/api/auth/github/callback";
        return ResponseEntity.status(302).header("Location", url).build();
    }

    @GetMapping("/github/callback")
    public ResponseEntity<Void> githubCallbackGet(@org.springframework.web.bind.annotation.RequestParam("code") String code, jakarta.servlet.http.HttpServletResponse httpServletResponse) {
        AuthResponse response = authService.processOAuthLogin(code);
        
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("auth_token", response.token());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 day
        httpServletResponse.addCookie(cookie);

        String redirectUrl = "http://localhost:4200/dashboard";
        return ResponseEntity.status(302).header("Location", redirectUrl).build();
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
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("auth_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
