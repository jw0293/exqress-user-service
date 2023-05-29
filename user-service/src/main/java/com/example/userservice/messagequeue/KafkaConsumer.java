package com.example.userservice.messagequeue;

import com.example.userservice.entity.QRcode;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.entity.state.FirstStateInfo;
import com.example.userservice.entity.state.LastStateInfo;
import com.example.userservice.entity.state.MiddleStateInfo;
import com.example.userservice.messagequeue.topic.KafkaTopic;
import com.example.userservice.repository.state.FirstStateInfoRepository;
import com.example.userservice.repository.state.LastStateInfoRepository;
import com.example.userservice.repository.state.MiddleStateInfoRepository;
import com.example.userservice.repository.QRcodeRepository;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private ModelMapper modelMapper;
    private ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final QRcodeRepository qRinfoRepository;
    private final LastStateInfoRepository lastStateInfoRepository;
    private final FirstStateInfoRepository firstStateInfoRepository;
    private final MiddleStateInfoRepository middleStateInfoRepository;

    @PostConstruct
    public void initMapper(){
        objectMapper = new ObjectMapper();
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }


    @KafkaListener(topics = KafkaTopic.QRINFO_FROM_ADMIN_SERVICE)
    public void setQRInfo(String kafkaMessage){
        log.info("Get QR Information : {}", kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        try{
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
        FirstStateInfo firstStateInfo = getFirstStateInfo(map);
        firstStateInfoRepository.save(firstStateInfo);

        QRcode qRcode = saveQRcode(map, firstStateInfo);
        firstStateInfo.connectQRInfo(qRcode);
    }

    @KafkaListener(topics = "delivery_start")
    public void connectDelivery(String kafkaMessage){
        log.info("Get KafkaListener :" + kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        try{
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
        MiddleStateInfo middleStateInfo = getMiddleStateInfo(map);
        QRcode qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
        qrInfo.setMiddleStateInfo(middleStateInfo);
        middleStateInfo.setQRinfo(qrInfo);
        middleStateInfoRepository.save(middleStateInfo);
        qRinfoRepository.save(qrInfo);
    }

    @KafkaListener(topics = "delivery_complete")
    public void connectDeliveryComplete(String kafkaMessage){
        log.info("Get KafkaListener Complete : {}", kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        try{
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
        LastStateInfo lastStateInfo = getLastStateInfo(map);
        QRcode qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
        qrInfo.setLastStateInfo(lastStateInfo);
        lastStateInfo.setQRinfo(qrInfo);
        lastStateInfoRepository.save(lastStateInfo);
        qRinfoRepository.save(qrInfo);
    }

    private FirstStateInfo getFirstStateInfo(Map<Object, Object> map){
        FirstStateInfo firstStateInfo = new FirstStateInfo();
        firstStateInfo.setCurState((String)map.get("curState"));

        return firstStateInfo;
    }

    private MiddleStateInfo getMiddleStateInfo(Map<Object, Object> map){
        MiddleStateInfo middleStateInfo = new MiddleStateInfo();
        log.info("State Delivery Name : {}", (String) map.get("deliveryName"));
        log.info("State Delivery Phone Number : {}", (String) map.get("deliveryPhoneNumber"));
        log.info("State Delivery State : {}", (String) map.get("state"));
        middleStateInfo.setDeliveryName((String) map.get("deliveryName"));
        middleStateInfo.setDeliveryPhoneNumber((String) map.get("deliveryPhoneNumber"));
        middleStateInfo.setCurState((String) map.get("state"));

        return middleStateInfo;
    }

    private LastStateInfo getLastStateInfo(Map<Object, Object> map){
        LastStateInfo lastStateInfo = new LastStateInfo();
        log.info("State Delivery Name : {}", (String) map.get("deliveryName"));
        log.info("State Delivery Phone Number : {}", (String) map.get("deliveryPhoneNumber"));
        log.info("State Delivery State : {}", (String) map.get("state"));
        lastStateInfo.setDeliveryName((String) map.get("deliveryName"));
        lastStateInfo.setDeliveryPhoneNumber((String) map.get("deliveryPhoneNumber"));
        lastStateInfo.setCurState((String) map.get("state"));

        return lastStateInfo;
    }

    private QRcode saveQRcode(Map<Object, Object> map, FirstStateInfo firstStateInfo){
        QRcode qRcode = new QRcode();
        qRcode.setQrId((String) map.get("qrId"));
        qRcode.setAddress((String) map.get("address"));
        qRcode.setInvoiceNo((String) map.get("invoiceNo"));
        qRcode.setIsComplete((String) map.get("curState"));
        qRcode.setProductName((String) map.get("productName"));
        qRcode.setAddress((String) map.get("address"));
        qRcode.setFirstStateInfo(firstStateInfo);

        UserEntity userEntity = userRepository.findByUserId((String) map.get("userId"));
        qRcode.setUserEntity(userEntity);
        qRinfoRepository.save(qRcode);

        userEntity.getQRinfoList().add(qRcode);
        userRepository.save(userEntity);

        return qRcode;
    }
}
