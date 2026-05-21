package com.karina.smeet.modules.auth.dto.response;

public record RefreshTokenResponse(
   //boolean authenticated,
   String accessToken,
   long accessTokenExpiryIn
) {}
