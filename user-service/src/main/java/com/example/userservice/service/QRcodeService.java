package com.example.userservice.service;

import com.example.userservice.StatusEnum;
import com.example.userservice.entity.QRcode;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.QRcodeRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.request.RequestQRcode;
import com.example.userservice.vo.request.RequestTemp;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseParcel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class QRcodeService {

    private ModelMapper mapper;
    private final QRcodeRepository qRcodeRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void initMapper(){
        mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }


    public ResponseEntity<ResponseData> scanQRcode(String userId, RequestQRcode requestQRcode) {
        QRcode qrInfo = qRcodeRepository.findByQrId(requestQRcode.getQrId());
        log.info("Find QR ID : {}", requestQRcode.getQrId());
        log.info("Find QRInfo : {}", qrInfo);
        log.info("Befoe Find User ID : {}", userId);
        UserEntity user = userRepository.findByUserId(userId);
        log.info("Find User ID : {}", user.getUserId());
        // 찾지 못한 경우 -> 반송 처리
        if(qrInfo == null) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.NOT_FOUND.getStatusCode(), "등록되지 않은 QR_ID입니다. 반송을 요청하십시오.", "", ""), HttpStatus.NOT_FOUND);
        }

        log.info("QRInfo With Connect User : {}", qrInfo.getUserEntity().getUserId());
        // 사용자에게 할당된 물품이 아닐 경우
        if(!qrInfo.getUserEntity().getUserId().equals(user.getUserId())){
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "로그인 한 회원이 주문한 상품이 아닙니다.", "", ""), HttpStatus.BAD_REQUEST);
        }

        ResponseParcel responseParcel = createResponseParcel(user, qrInfo);
        log.info("ResponseParcel :{}", responseParcel);
//        ResponseQRcodeInto responseQRcodeInto = mapper.map(qrInfo, ResponseQRcodeInto.class);
//        responseQRcodeInto.setReceiverName(user.getName());
//        responseQRcodeInto.setPhoneNumber(user.getPhoneNumber());
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "회원이 주문한 상품이 배송 완료되었습니다.",  responseParcel, ""), HttpStatus.OK);
    }

    private ResponseParcel createResponseParcel(UserEntity user, QRcode qRcode){
        ResponseParcel responseParcel = new ResponseParcel();
        responseParcel.setCreatedDate(qRcode.getCreatedAt().toString());
        responseParcel.setInvoiceNo(qRcode.getInvoiceNo());
        responseParcel.setReceiverName(user.getName());
        responseParcel.setState(qRcode.getState());
        responseParcel.setProductName(qRcode.getProductName());
        responseParcel.setAddress(qRcode.getAddress());
        responseParcel.setCompany(qRcode.getCompany());
        responseParcel.setDeliveryName(qRcode.getMiddleStateInfo().getDeliveryName());

        return responseParcel;
    }
}
