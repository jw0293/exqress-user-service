package com.example.userservice.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RequestQRcode {

    @Schema(description = "QR ID", nullable = false, example = "dsjsdtkjdope4238genfj")
    private String qrId;

}
