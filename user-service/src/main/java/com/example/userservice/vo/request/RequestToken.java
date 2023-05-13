package com.example.userservice.vo.request;

import lombok.Data;

@Data
public class RequestToken {

    private String accessToken;
    private String refreshToken;

}
