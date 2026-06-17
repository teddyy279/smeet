package com.karina.smeet.modules.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutboundTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") Long expiresIn,
    @JsonProperty("refresh_token") String refreshToken,
    String scope,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("id_token") String idToken
) {}
