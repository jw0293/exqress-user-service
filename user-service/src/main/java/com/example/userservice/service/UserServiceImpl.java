package com.example.userservice.service;

import com.example.userservice.StatusEnum;
import com.example.userservice.constants.AuthConstants;
import com.example.userservice.dto.TokenInfo;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.utils.CookieUtils;
import com.example.userservice.utils.TokenUtils;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.response.ResponseData;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{

    private final Environment env;
    private final TokenUtils tokenUtils;
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    // private final DeliveryServiceClient deliveryServiceClient;

    private ModelMapper mapper;

    @PostConstruct
    public void initMapper(){
        mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

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
    public ResponseEntity<ResponseData> login(HttpServletRequest request, HttpServletResponse response, RequestLogin login) {
        UserEntity entity = userRepository.findByEmail(login.getEmail());
        if(entity == null){
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "존재하지 않는 배송기사 이메일입니다.", "", ""), HttpStatus.BAD_REQUEST);
        }
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = login.toAuthentication();

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        //Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        UserDetails userDetails = loadUserByUsername(authenticationToken.getName());
        if(userDetails == null){
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "존재하지 않는 배송기사 이메일입니다.", "", ""), HttpStatus.BAD_REQUEST);
        }

        if(!bCryptPasswordEncoder.matches(login.getPassword(), entity.getEncryptedPwd())) {
            log.error("비밀번호오류");
            return new ResponseEntity<>(new ResponseData(StatusEnum.Unauthorized.getStatusCode(), "비밀번호 오류입니다.", "", ""), HttpStatus.UNAUTHORIZED);
        }

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = tokenUtils.generateToken(entity.getUserId());

        log.info("Access : {}", tokenInfo.getAccessToken());
        log.info("Refresh :{}", tokenInfo.getRefreshToken());

        Cookie cookie = cookieUtils.createCookie(AuthConstants.REFRESH_HEADER, tokenInfo.getRefreshToken());
        response.addCookie(cookie);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        log.info("Cookie에 담김");

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + entity.getUserId(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "로그인 성공", "", tokenInfo.getAccessToken()), HttpStatus.OK);
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
}
