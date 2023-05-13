package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.vo.request.RequestToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto createUser(UserDto userDto);
    UserDto getUserDetailsByEmail(String email);

    UserDto getUserByUserId(String userId);
    ResponseEntity<?> reissue(RequestToken tokenInfo);
    ResponseEntity<?> logout(RequestToken tokenInfo);
    boolean isDuplicated(String email);
}
