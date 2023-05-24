package com.example.userservice.vo.request;

import lombok.Data;

@Data
public class RequestTemp {

    private String userId;
    private String qrId;
    private String productName;
    private String invoiceNo;
    private String state;
}
