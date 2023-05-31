package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface TokenService {

    String createToken(UserDto userDto);
    ResponseEntity<ResponseData> logout(String accessToken);
    ResponseEntity<ResponseData> reissue(HttpServletRequest request, HttpServletResponse response);
    String getAccessToken(HttpServletRequest request, HttpServletResponse response);

}
