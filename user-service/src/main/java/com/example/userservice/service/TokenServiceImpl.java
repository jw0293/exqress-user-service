package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final Environment env;

    @Override
    public String createToken(UserDto userDto){

        log.info("Token Expiration_time : {}", env.getProperty("token.expiration_time"));
        log.info("Token Secret : {}", env.getProperty("token.secret"));

        return Jwts.builder()
                .setSubject(userDto.getUserId())
                .setExpiration(
                        new Date(System.currentTimeMillis() +
                                Long.parseLong(env.getProperty("token.expiration_time")))
                )
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();
    }
}
