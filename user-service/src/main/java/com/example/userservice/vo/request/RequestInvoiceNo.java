package com.example.userservice.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RequestInvoiceNo {
    @Schema(description = "운송장 번호", nullable = false, example = "2931753")
    private String invoiceNo;
}
