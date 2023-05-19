package com.example.userservice.vo.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResponseError {

    @Schema(description = "오류 코드", nullable = false)
    private String errorCode;

    @Schema(description = "오류 메세지", nullable = false)
    private String errorMessage;

    @Schema(description = "데이터", nullable = false)
    private Object Data;

    @Schema(description = "Access Token", nullable = false)
    private String accessToken;
}
