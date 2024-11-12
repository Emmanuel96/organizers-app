package com.calgary.organizers.organizersapp.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Integer integer,
    @JsonProperty("token_type") String tokenType
) {}
