package com.example.userservice.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResponseParcel {

    @Schema(description = "운송장 번호", nullable = false, example = "120123952")
    private String invoiceNo;

    @Schema(description = "물품의 상품 이름", nullable = false, example = "호박 고구마")
    private String productName;

    @Schema(description = "배송 완료 여부", nullable = false, example = "false")
    private Boolean isComplete;
}
