package com.example.userservice.service;

import com.example.userservice.StatusEnum;
import com.example.userservice.constants.AuthConstants;
import com.example.userservice.dto.TokenInfo;
import com.example.userservice.dto.UserDto;
import com.example.userservice.utils.CookieUtils;
import com.example.userservice.utils.TokenUtils;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final Environment env;
    private final TokenUtils tokenUtils;
    private final CookieUtils cookieUtils;
    private final RedisTemplate redisTemplate;

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

    @Override
    public ResponseEntity<ResponseData> reissue(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = cookieUtils.getCookie(request, AuthConstants.REFRESH_HEADER);
        String refreshToken = refreshCookie.getValue();
        log.info("Reissue Refresh Token : {}", refreshToken);
        // 1. Refresh Token 검증
        if (!tokenUtils.isValidToken(refreshToken)) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "Refresh 토큰이 유효하지 않습니다.", "", ""), HttpStatus.BAD_REQUEST);
        }

        log.info("유효한 토큰 확인");
        // 2. Access Token 에서 User email 을 가져옵니다.
        ResponseUser authenticationUser = tokenUtils.getAuthentication(refreshToken);

        log.info("AuthUser Name : {}", authenticationUser.getName());
        log.info("AuthUser Email : {}", authenticationUser.getEmail());
        log.info("AuthUser UserId : {}", authenticationUser.getUserId());

        // 3. Redis 에서 User email 을 기반으로 저장된 Refresh Token 값을 가져옵니다.
        String refreshTokenFromRedis = (String) redisTemplate.opsForValue().get("RT:" + authenticationUser.getUserId());
        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if(ObjectUtils.isEmpty(refreshTokenFromRedis)) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "잘못된 요청입니다.", "", ""), HttpStatus.BAD_REQUEST);
        }
        if(!refreshTokenFromRedis.equals(refreshToken)) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "Refresh 토큰이 일치하지 않습니다.", "", ""), HttpStatus.BAD_REQUEST);
        }

        // 4. 새로운 토큰 생성
        TokenInfo newTokenInfo = tokenUtils.generateToken(authenticationUser.getUserId());
        log.info("New Token Success !");

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authenticationUser.getUserId(), newTokenInfo.getRefreshToken(), newTokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        Cookie cookie = cookieUtils.createCookie(AuthConstants.REFRESH_HEADER, newTokenInfo.getRefreshToken());
        response.addCookie(cookie);
        response.setContentType("application/json;charset=UTF-8");

        log.info("New 토큰 반환");
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "Token 정보가 갱신되었습니다.", "", newTokenInfo.getAccessToken()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseData> logout(String accessToken) {
        // 1. Access Token 검증
        if (!tokenUtils.isValidToken(accessToken)) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "잘못된 요청입니다.", "", ""), HttpStatus.BAD_REQUEST);
        }
        log.info("유효한 토큰 확인");
        // 2. Access Token 에서 User email 을 가져옵니다.
        ResponseUser authentication = tokenUtils.getAuthentication(accessToken);

        log.info("AuthUser Name : {}", authentication.getName());
        log.info("AuthUser Email : {}", authentication.getEmail());
        log.info("AuthUser UserId : {}", authentication.getUserId());

        // 3. Redis 에서 해당 User ID로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제합니다.
        if (redisTemplate.opsForValue().get("RT:" + authentication.getUserId()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getUserId());
        }

        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = tokenUtils.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "로그아웃 되었습니다.", "", ""), HttpStatus.OK);
    }

    @Override
    public String getAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return request.getHeader(AuthConstants.AUTHORIZATION_HEADER).substring(AuthConstants.TOKEN_TYPE.length());
    }
}
