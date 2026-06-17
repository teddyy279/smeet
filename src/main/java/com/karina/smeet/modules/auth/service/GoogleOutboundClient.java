package com.karina.smeet.modules.auth.service;

import com.karina.smeet.modules.auth.dto.request.OutboundTokenRequest;
import com.karina.smeet.modules.auth.dto.response.OutboundTokenResponse;
import com.karina.smeet.modules.auth.dto.response.OutboundUserInfoResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOutboundClient {
    private final RestClient restClient = RestClient.create();

    public OutboundTokenResponse exchangeToken(OutboundTokenRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", request.code());
        body.add("client_id", request.clientId());
        body.add("client_secret", request.clientSecret());
        body.add("redirect_uri", request.redirectUri());
        body.add("grant_type", request.grantType());

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(OutboundTokenResponse.class); // google return JSON OutboundTokenResponse và Spring tự map sang ... record của nó
    }

    public OutboundUserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(OutboundUserInfoResponse.class);
    }
}
