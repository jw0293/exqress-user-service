package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto createUser(UserDto userDto);
    UserDto getUserDetailsByEmail(String email);

    UserDto getUserByUserId(String userId);
    boolean isDuplicated(String email);
    ResponseEntity<ResponseData> login(HttpServletRequest request, HttpServletResponse response, RequestLogin login);
}
