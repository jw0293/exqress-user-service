package com.example.userservice.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResponseQRcodeInto {

    @Schema(description = "운송장 번호", nullable = false, example = "120123952")
    private String invoiceNo;

    @Schema(description = "수령인 이름", nullable = false, example = "심청이")
    private String name;

    @Schema(description = "수령인 이름", nullable = false, example = "심청이")
    private String receiverName;

    @Schema(description = "수령인 전화번호", nullable = false, example = "010-2213-5123")
    private String phoneNumber;
}
