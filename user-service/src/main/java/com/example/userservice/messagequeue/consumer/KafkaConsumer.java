package com.example.userservice.messagequeue.consumer;

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
        qRcode.setState("ready");
        firstStateInfo.connectQRInfo(qRcode);
    }

    @KafkaListener(topics = KafkaTopic.DELIVERY_START)
    public void connectDelivery(String kafkaMessage){
        log.info("Get KafkaListener :" + kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        try{
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException ex){
            ex.printStackTrace();
        }
        MiddleStateInfo middleStateInfo = getMiddleStateInfo(map);
        middleStateInfoRepository.save(middleStateInfo);

        QRcode qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
        qrInfo.setState("start");
        qrInfo.setMiddleStateInfo(middleStateInfo);

        middleStateInfo.assignQRInfo(qrInfo);
        qRinfoRepository.save(qrInfo);
    }

    @KafkaListener(topics = KafkaTopic.DELIVERY_COMPLETE)
    public void connectDeliveryComplete(String kafkaMessage){
        log.info("Get KafkaListener Complete : {}", kafkaMessage);
        Map<Object, Object> map = new HashMap<>();
        try{
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
        LastStateInfo lastStateInfo = getLastStateInfo(map);
        lastStateInfoRepository.save(lastStateInfo);

        QRcode qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
        qrInfo.setLastStateInfo(lastStateInfo);
        qrInfo.setState("complete");

        lastStateInfo.assignQRcode(qrInfo);
        qRinfoRepository.save(qrInfo);
    }

    private FirstStateInfo getFirstStateInfo(Map<Object, Object> map){
        FirstStateInfo firstStateInfo = new FirstStateInfo();
        firstStateInfo.setQrId((String) map.get("qrId"));
        firstStateInfo.setCurState((String)map.get("curState"));

        return firstStateInfo;
    }

    private MiddleStateInfo getMiddleStateInfo(Map<Object, Object> map){
        MiddleStateInfo middleStateInfo = new MiddleStateInfo();
        middleStateInfo.setQrId((String) map.get("qrId"));
        middleStateInfo.setDeliveryName((String) map.get("deliveryName"));
        middleStateInfo.setDeliveryPhoneNumber((String) map.get("deliveryPhoneNumber"));
        middleStateInfo.setCurState((String) map.get("state"));

        return middleStateInfo;
    }

    private LastStateInfo getLastStateInfo(Map<Object, Object> map){
        LastStateInfo lastStateInfo = new LastStateInfo();
        lastStateInfo.setQrId((String) map.get("qrId"));
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
        qRcode.setState((String) map.get("curState"));
        qRcode.setProductName((String) map.get("productName"));
        qRcode.setAddress((String) map.get("address"));
        qRcode.setCompany((String) map.get("company"));
        qRcode.setFirstStateInfo(firstStateInfo);

        UserEntity userEntity = userRepository.findByUserId((String) map.get("userId"));
        qRcode.setUserEntity(userEntity);
        qRinfoRepository.save(qRcode);

        userEntity.getQRinfoList().add(qRcode);
        userRepository.save(userEntity);

        return qRcode;
    }
}
