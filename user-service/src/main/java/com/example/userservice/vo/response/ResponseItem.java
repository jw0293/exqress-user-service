package com.example.userservice.vo.response;

import lombok.Data;

@Data
public class ResponseItem {
    private String invoiceNo;
    private String productName;
    private String receiverName;
    private String receiverPhoneNumber;
    private Boolean isComplete;
}
