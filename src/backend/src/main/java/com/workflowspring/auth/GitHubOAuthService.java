package com.workflowspring.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class GitHubOAuthService {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public GitHubOAuthService(
            @Value("${github.client-id}") String clientId,
            @Value("${github.client-secret}") String clientSecret) {
        this.restTemplate = new RestTemplate();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        GitHubAccessTokenResponse response = restTemplate.postForObject(
                "https://github.com/login/oauth/access_token",
                request,
                GitHubAccessTokenResponse.class);

        if (response == null || response.access_token() == null) {
            throw new RuntimeException("Failed to exchange code for access token");
        }

        return response.access_token();
    }

    public GitHubUser fetchUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GitHubUser> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                request,
                GitHubUser.class);

        return response.getBody();
    }

    public List<GitHubEmail> fetchEmails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GitHubEmail[]> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                request,
                GitHubEmail[].class);

        if (response.getBody() == null) {
            return List.of();
        }

        return Arrays.asList(response.getBody());
    }

    public record GitHubAccessTokenResponse(String access_token, String token_type, String scope) {}

    public record GitHubUser(String login, Long id, String name, String email, String avatar_url) {
        public String avatarUrl() {
            return avatar_url;
        }
    }

    public record GitHubEmail(String email, boolean primary, boolean verified, String visibility) {}
}
