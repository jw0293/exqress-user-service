package com.example.userservice.messagequeue.topic;

public interface KafkaTopic {
    String CREATE_USER = "create_user";
    String DELIVERY_START = "delivery_start";
    String DELIVERY_COMPLETE = "delivery_complete";
    String QRINFO_FROM_ADMIN_SERVICE = "QRInfo_to_userservice";
}
