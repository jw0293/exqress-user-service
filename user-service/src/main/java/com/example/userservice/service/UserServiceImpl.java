package com.example.userservice.service;

import com.example.userservice.StatusEnum;
import com.example.userservice.dto.TokenInfo;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.utils.TokenUtils;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{

    private final Environment env;
    private final RedisTemplate redisTemplate;
    private final TokenUtils tokenUtils;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    // private final DeliveryServiceClient deliveryServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user =  userRepository.findByEmail(username);

        if(user == null)
            throw new UsernameNotFoundException(username);

        return new User(user.getEmail(), user.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null){
            throw new IllegalArgumentException();
        }

        return new ModelMapper().map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){
            throw new UsernameNotFoundException("User not found!");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        /** Using RestTemplate **/
//        String deliveryUrl = String.format(env.getProperty("order_service.url"), userId);
//        ResponseEntity<List<ResponseItem>> itemListResponse =
//                restTemplate.exchange(deliveryUrl, HttpMethod.GET, null,
//                            new ParameterizedTypeReference<List<ResponseItem>>() {
//                });

        /** Using a feign client **/
        // List<ResponseItem> itemList = deliveryServiceClient.getItems(userId);
        //userDto.setItems(itemList);

        return userDto;
    }

    @Override
    public boolean isDuplicated(String email) {
        if(userRepository.findByEmail(email) == null) {
            return false;
        }
        return true;
    }

    @Override
    public ResponseEntity<ResponseData> reissue(RequestToken tokenInfo) {
        // 1. Refresh Token 검증
        if (!tokenUtils.isValidToken(tokenInfo.getRefreshToken())) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "Refresh 토큰이 유효하지 않습니다.", ""), HttpStatus.BAD_REQUEST);
        }

        log.info("유효한 토큰 확인");
        // 2. Access Token 에서 User email 을 가져옵니다.
        ResponseUser authenticationUser = tokenUtils.getAuthentication(tokenInfo.getAccessToken());

        log.info("AuthUser Name : {}", authenticationUser.getName());
        log.info("AuthUser Email : {}", authenticationUser.getEmail());
        log.info("AuthUser UserId : {}", authenticationUser.getUserId());

        // 3. Redis 에서 User email 을 기반으로 저장된 Refresh Token 값을 가져옵니다.
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authenticationUser.getName());
        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if(ObjectUtils.isEmpty(refreshToken)) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "잘못된 요청입니다.", ""), HttpStatus.BAD_REQUEST);
        }
        if(!refreshToken.equals(tokenInfo.getRefreshToken())) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "Refresh 토큰이 일치하지 않습니다.", ""), HttpStatus.BAD_REQUEST);
        }

        // 4. 새로운 토큰 생성
        TokenInfo newTokenInfo = tokenUtils.generateToken(authenticationUser.getUserId());
        log.info("New Token Success !");

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authenticationUser.getUserId(), newTokenInfo.getRefreshToken(), newTokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        log.info("New 토큰 반환");
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "Token 정보가 갱신되었습니다.", newTokenInfo), HttpStatus.OK);
    }
}
