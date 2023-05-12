package com.example.userservice;

public enum StatusEnum {
    OK("OK", 200),
    BAD_REQUEST("BAD_REQUEST", 400),
    NOT_FOUND("NOT_FOUND", 404),
    EXISTED("AlREADY_EXIST_DATA", 409),
    INTERNAL_SEER_ERROR("INTERNAL_SERVER_ERROR", 500);

    String code;
    int statusCode;

    StatusEnum(String code, int statusCode) {
        this.code = code;
        this.statusCode = statusCode;
    }
}
