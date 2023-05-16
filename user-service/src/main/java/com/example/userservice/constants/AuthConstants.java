package com.example.userservice.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTH_HEADER = "accessToken";
    public static final String REFRESH_HEADER = "refreshToken";
    public static final String TOKEN_TYPE = "Bearer";
}
