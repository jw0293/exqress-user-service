package com.example.userservice.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseLogin {

    private String token;
    private String userId;
}
