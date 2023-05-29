package com.example.userservice.messagequeue.topic;

public interface KafkaTopic {
    String CREATE_USER = "create_user";
    String QRINFO_FROM_ADMIN_SERVICE = "QRInfo_to_userservice";
}
