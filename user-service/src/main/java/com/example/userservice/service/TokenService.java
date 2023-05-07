package com.example.userservice.service;

import com.example.userservice.dto.UserDto;

public interface TokenService {

    String createToken(UserDto userDto);
}
