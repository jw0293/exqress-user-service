package com.example.userservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
public class Token {

    private String accessToken;
    private String refreshToken;
    private Long refreshTokenExpirationTime;

    public Token(String accessToken, String refreshToken, Long refreshTokenExpirationTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }
}
