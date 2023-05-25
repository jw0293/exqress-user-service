package com.example.userservice.messagequeue;

import com.example.userservice.entity.QRinfo;
import com.example.userservice.entity.state.LastStateInfo;
import com.example.userservice.entity.state.MiddleStateInfo;
import com.example.userservice.repository.state.LastStateInfoRepository;
import com.example.userservice.repository.state.MiddleStateInfoRepository;
import com.example.userservice.repository.QRinfoRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.kafkaDto.QRdto;
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
    private final QRinfoRepository qRinfoRepository;
    private final LastStateInfoRepository lastStateInfoRepository;
    private final MiddleStateInfoRepository middleStateInfoRepository;

    @PostConstruct
    public void initMapper(){
        objectMapper = new ObjectMapper();
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }


//    @KafkaListener(topics = "TrackingInfoId")
//    public void updateTrackingInfo(String kafkaMessage){
//        log.info("Kafka Message: -> " + kafkaMessage);
//
//        Map<Object, Object> map = new HashMap<>();
//        try{
//            map = modelMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
//            /*
//            map으로 Admin에서 전송한 정보를 추출해야함
//            ------------------
//            1) userId
//            2) qrId
//            3) invoiceNo
//            4) productName
//            5) receiverName (= 본인 이름이겠지?)
//            ------------------
//            전송 받아서 연관관계 설정해주고 데이터베이스에 저장해준다
//             */
//        } catch (JsonProcessingException ex){
//            // 예외 처리 해줘야함
//            ex.printStackTrace();
//        }
//        UserEntity user = userRepository.findByUserId((String) map.get("userId"));
//        if(user != null){
//            QRdto qrDto = getQrDto(map);
//            QRinfo kafkaQRInfo = modelMapper.map(qrDto, QRinfo.class);
//
//            // 도메인 주도 설계 기법으로 구현
//            user.addQRinfoList(kafkaQRInfo);
//            kafkaQRInfo.connectUser(user);
//        }
//    }

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
        QRinfo qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
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
        QRinfo qrInfo = qRinfoRepository.findByQrId((String)map.get("qrId"));
        qrInfo.setLastStateInfo(lastStateInfo);
        lastStateInfo.setQRinfo(qrInfo);
        lastStateInfoRepository.save(lastStateInfo);
        qRinfoRepository.save(qrInfo);
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

    public QRdto getQrDto(Map<Object, Object> map){
        QRdto qRdto = new QRdto();
        qRdto.setQrId((String) map.get("qrId"));
        qRdto.setInvoiceNo((String) map.get("invoiceNo"));
        qRdto.setProductName((String) map.get("productName"));

        return qRdto;
    }
}
