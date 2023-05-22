package com.example.userservice.kafkaDto;

import lombok.Data;

@Data
public class DeliveryInfoWithQRid {

    private String qrId;
    private String deliveryName;
    private String deliveryPhoneNumber;
    private String curState;
}
