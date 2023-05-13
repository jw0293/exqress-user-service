package com.example.userservice.utils;

import com.example.userservice.dto.TokenInfo;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.response.ResponseUser;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtils {

    private final Environment env;
    private static String secretKey;
    private final UserRepository userRepository;

    @PostConstruct
    protected void init(){
        secretKey = env.getProperty("token.secret");
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public TokenInfo generateToken(String uid){

        Claims claims = Jwts.claims().setSubject(uid);

        Date now = new Date();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(System.currentTimeMillis() +
                Long.parseLong(env.getProperty("token.access_expiration_time")));

        log.info("AccessTokenExpiresIn : {}", accessTokenExpiresIn);

        String accessToken = Jwts.builder()
                .setSubject(uid)
                .setExpiration(accessTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();


        Date refreshExpirationTime = new Date(System.currentTimeMillis() +
                Long.parseLong(env.getProperty("token.refresh_expiration_time")));

        log.info("RefreshExpirationTime : {}", refreshExpirationTime);

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(uid)
                .setExpiration(refreshExpirationTime)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(Long.parseLong(env.getProperty("token.refresh_expiration_time")))
                .build();
    }

    public boolean isValidToken(String token){
        try {
            log.info("Valid Token : {}", token);
            Date expiration = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
            log.info("Get expiration : {}",expiration.getTime());
            Long now = System.currentTimeMillis();
            if(expiration.getTime() - now > 0) {
                return true;
            }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public ResponseUser getAuthentication(String token){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity userByEmail = userRepository.findByUserId(Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject());

        return mapper.map(userByEmail, ResponseUser.class);
    }

    public Long getExpiration(String token){
        // token 남은 유효 시간
        Date expiration = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
        // 현재 시간
        Long now = System.currentTimeMillis();
        return (expiration.getTime() - now);
    }

}
