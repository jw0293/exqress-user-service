package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.kafkaDto.KafkaCreateUser;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.request.RequestQRcode;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService{

    UserDto createUser(UserDto userDto);
    boolean isDuplicated(String email);

    String getUserIdThroughRequest(HttpServletRequest request);
    KafkaCreateUser createKafkaUser(UserDto userDto);
    ResponseEntity<ResponseData> login(HttpServletRequest request, HttpServletResponse response, RequestLogin login);

    ResponseEntity<ResponseData> getQRList(String userId);
    ResponseEntity<ResponseData> requestReturnParcel(String qrId);
    ResponseEntity<ResponseData> clearPrivateInformation(String userId, String qrId);
}
