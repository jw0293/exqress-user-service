package com.example.userservice.messagequeue.producer;

import com.example.userservice.kafkaDto.KafkaCreateUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaProducer {

    private ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    private void initMapper(){
        objectMapper = new ObjectMapper();
    }

    public void sendCreateUser(String kafkaTopic, KafkaCreateUser kafkaCreateUser){
        String jsonInString = "";
        try{
            jsonInString = objectMapper.writeValueAsString(kafkaCreateUser);
        } catch (JsonProcessingException e){
            e.printStackTrace();;
        }
        kafkaTemplate.send(kafkaTopic, jsonInString);
    }

}
