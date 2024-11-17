package com.calgary.organizers.organizersapp.service.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerFlowProvider {

    @Value("${spring.security.oauth2.client.provider.meetup.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.meetup.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.meetup.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.meetup.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Set headers for the token request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Populate the body parameters for the token request
        HttpEntity<MultiValueMap<String, String>> request = getMultiValueMapHttpEntity(code, headers);

        // Exchange code for access token
        ResponseEntity<AccessTokenResponse> tokenResponse = restTemplate.postForEntity(tokenUri, request, AccessTokenResponse.class);

        if (tokenResponse.getStatusCode() == HttpStatus.OK && tokenResponse.getBody() != null) {
            // Get the access token from the response
            String accessToken = tokenResponse.getBody().accessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Failed to retrieve access token.");
            }
        }
        return tokenResponse.getBody().accessToken();
    }

    private HttpEntity<MultiValueMap<String, String>> getMultiValueMapHttpEntity(String code, HttpHeaders headers) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("client_id", clientId);
        requestParams.add("client_secret", clientSecret);
        requestParams.add("grant_type", "authorization_code");
        requestParams.add("redirect_uri", redirectUri);
        requestParams.add("code", code);

        return new HttpEntity<>(requestParams, headers);
    }
}
