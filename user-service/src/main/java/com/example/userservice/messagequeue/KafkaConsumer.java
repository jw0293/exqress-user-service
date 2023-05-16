package com.example.userservice.messagequeue;

import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "UserMapItem")
    public void updateTrackingInfo(String kafkaMessage){
        log.info("Kafka Message: -> " + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try{
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }

        UserEntity user = userRepository.findByUserId((String) map.get("userId"));
        if(user != null){
            //user.setTrackingInfo();
            userRepository.save(user);
        }
    }
}
