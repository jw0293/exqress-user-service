package com.example.userservice.vo.response;

import com.example.userservice.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseData {

    private String status;
    private String message;
    private Object data;
    private String accessToken;
}
