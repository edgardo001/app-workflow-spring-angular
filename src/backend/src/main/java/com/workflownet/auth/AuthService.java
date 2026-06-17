package com.workflownet.auth;

import com.workflownet.auth.GitHubOAuthService.GitHubEmail;
import com.workflownet.auth.GitHubOAuthService.GitHubUser;
import com.workflownet.auth.infrastructure.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GitHubOAuthService gitHubOAuthService;
    private final JwtTokenService jwtTokenService;
    private final UserProfileRepository userProfileRepository;

    public AuthResponse processOAuthLogin(String code) {
        String accessToken = gitHubOAuthService.exchangeCodeForToken(code);
        GitHubUser gitHubUser = gitHubOAuthService.fetchUser(accessToken);
        List<GitHubEmail> emails = gitHubOAuthService.fetchEmails(accessToken);

        String email = emails.stream()
                .filter(GitHubEmail::primary)
                .findFirst()
                .map(GitHubEmail::email)
                .orElse(gitHubUser.login() + "@users.noreply.github.com");

        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseGet(UserProfile::new);

        user.setEmail(email);
        user.setName(gitHubUser.name() != null ? gitHubUser.name() : gitHubUser.login());
        user.setAvatarUrl(gitHubUser.avatarUrl());
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        userProfileRepository.save(user);

        String token = jwtTokenService.generateAuthToken(user);
        return new AuthResponse(token, user);
    }

    public UserProfile getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserProfile fetchProfile(String email) {
        return userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
