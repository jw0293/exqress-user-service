//package com.example.userservice.messagequeue;
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//
//import java.util.*;
//
////@EnableKafka
////@Configuration
//public class KafkaConsumerConfig {
//
//    // 접속하고자 하는 정보가 됨
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory(){
//        Map<String, Object> properties = new HashMap<>();
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
//        // GroupId는 Kafka에 Topic에 쌓여있는 메세지를 가져갈 수 있는 Consumer를 Grouping
//        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "TrackingInfoId");
//        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//
//        return new DefaultKafkaConsumerFactory<>(properties);
//    }
//
//    // 위 사항을 반영하여 실제 Listener
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(){
//        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        //kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
//
//        return kafkaListenerContainerFactory;
//    }
//
//}
