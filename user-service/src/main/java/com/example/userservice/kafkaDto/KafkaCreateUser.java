package com.example.userservice.kafkaDto;

import lombok.Data;

@Data
public class KafkaCreateUser {
    private String name;
    private String userId;
    private String phoneNumber;
}
