package com.example.userservice.kafkaDto;

import lombok.Data;

@Data
public class QRdto {

    private String qrId;
    private String invoiceNo;
    private String productName;
}
