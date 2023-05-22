//package com.example.userservice.messagequeue;
//
//import com.example.userservice.entity.QRinfo;
//import com.example.userservice.entity.UserEntity;
//import com.example.userservice.repository.QRinfoRepository;
//import com.example.userservice.repository.UserRepository;
//import com.example.userservice.kafkaDto.QRdto;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.modelmapper.ModelMapper;
//import org.modelmapper.convention.MatchingStrategies;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class KafkaConsumer {
//
//    private ModelMapper modelMapper;
//    private final UserRepository userRepository;
//    private final QRinfoRepository qRinfoRepository;
//
//    @PostConstruct
//    public void initMapper(){
//        modelMapper = new ModelMapper();
//        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//    }
//
//
//    @KafkaListener(topics = "UserMapItem")
//    public void updateTrackingInfo(String kafkaMessage){
//        log.info("Kafka Message: -> " + kafkaMessage);
//
//        Map<Object, Object> map = new HashMap<>();
//        ObjectMapper mapper = new ObjectMapper();
//        try{
//            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
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
//
//    public QRdto getQrDto(Map<Object, Object> map){
//        QRdto qRdto = new QRdto();
//        qRdto.setQrId((String) map.get("qrId"));
//        qRdto.setInvoiceNo((String) map.get("invoiceNo"));
//        qRdto.setProductName((String) map.get("productName"));
//
//        return qRdto;
//    }
//}
