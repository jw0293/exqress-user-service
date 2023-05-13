package com.example.userservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter @Builder
public class TokenInfo {

    private String accessToken;
    private String refreshToken;
    private Long refreshTokenExpirationTime;

    public TokenInfo(String accessToken, String refreshToken, Long refreshTokenExpirationTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }
}
