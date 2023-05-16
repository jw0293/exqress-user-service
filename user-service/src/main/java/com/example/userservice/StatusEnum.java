package com.example.userservice;

import lombok.Getter;

@Getter
public enum StatusEnum {
    OK("OK", "200"),
    NON_AUTHORITATIVE_INFORMATION("NON_AUTHORITATIVE_INFORMATION", "203"),
    BAD_REQUEST("BAD_REQUEST", "400"),
    Unauthorized("NOT_AUTHORIZED", "401"),
    NOT_FOUND("NOT_FOUND", "404"),
    EXISTED("AlREADY_EXIST_DATA", "409"),
    INTERNAL_SEER_ERROR("INTERNAL_SERVER_ERROR", "500");

    String code;
    String statusCode;

    StatusEnum(String code, String statusCode) {
        this.code = code;
        this.statusCode = statusCode;
    }
}
