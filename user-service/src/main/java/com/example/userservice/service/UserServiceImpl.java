package com.example.userservice.service;

import com.example.userservice.StatusEnum;
import com.example.userservice.constants.AuthConstants;
import com.example.userservice.dto.TokenInfo;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.QRcode;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.QRcodeRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.utils.CookieUtils;
import com.example.userservice.utils.TokenUtils;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.request.RequestQRcode;
import com.example.userservice.vo.request.RequestTemp;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseParcel;
import com.example.userservice.vo.response.ResponseQRcodeInto;
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
import java.util.List;
import java.util.Objects;
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
    private final QRcodeRepository qRinfoRepository;
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
        UserEntity user = userRepository.findByEmail(username);

        if(user == null) {
            //throw new UsernameNotFoundException("Not");
            //throw new CustomException(UNAUTHORIZED_MEMBER);
            return null;
        }

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

        log.info("login Pwd : {}", login.getPassword());
        log.info("Entity Pwd : {}", entity.getEncryptedPwd());
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
        userEntity.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPassword()));
        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public ResponseEntity<ResponseData> getQRList(String userID) {
        UserEntity user = userRepository.findByUserId(userID);
        List<QRcode> qRinfoList = user.getQRinfoList();
        List<ResponseParcel> parcels = createResponseParcelList(user, qRinfoList);

        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "사용자가 주문한 물품들입니다.",  parcels, ""), HttpStatus.OK);
    }

    private List<ResponseParcel> createResponseParcelList(UserEntity user, List<QRcode> qRinfoList){
        List<ResponseParcel> parcels = new ArrayList<>();
        for (QRcode qRinfo : qRinfoList) {
            parcels.add(createResponseParcel(user, qRinfo));
        }
        return parcels;
    }

    private ResponseParcel createResponseParcel(UserEntity user, QRcode qRcode){
        ResponseParcel responseParcel = new ResponseParcel();
        responseParcel.setCreatedDate(qRcode.getCreatedAt().toString());
        responseParcel.setInvoiceNo(qRcode.getInvoiceNo());
        responseParcel.setReceiverName(user.getName());
        if(qRcode.getLastStateInfo() == null) responseParcel.setIsComplete("false");
        else responseParcel.setIsComplete(qRcode.getIsComplete());
        responseParcel.setProductName(qRcode.getProductName());
        responseParcel.setReceiverPhoneNumber(user.getPhoneNumber());

        return responseParcel;
    }

    @Override
    public String getUserIdThroughRequest(HttpServletRequest request) {
        String author = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        String token = author.substring(7, author.length());
        return tokenUtils.getAuthentication(token).getUserId();
    }

    @Override
    public ResponseEntity<ResponseData> scanQRcode(String userId, RequestQRcode requestQRcode) {
        QRcode qrInfo = qRinfoRepository.findByQrId(requestQRcode.getQrId());
        log.info("Find QR ID : {}", requestQRcode.getQrId());
        log.info("Find QRInfo : {}", qrInfo);
        log.info("Befoe Find User ID : {}", userId);
        UserEntity user = userRepository.findByUserId(userId);
        log.info("Find User ID : {}", user.getUserId());
        // 찾지 못한 경우 -> 반송 처리
        if(qrInfo == null) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.NOT_FOUND.getStatusCode(), "등록되지 않은 QR_ID입니다. 반송을 요청하십시오.", "", ""), HttpStatus.NOT_FOUND);
        }

        log.info("QRInfo With Connect User : {}", qrInfo.getUserEntity().getUserId());
        // 사용자에게 할당된 물품이 아닐 경우
        if(!qrInfo.getUserEntity().getUserId().equals(user.getUserId())){
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "로그인 한 회원이 주문한 상품이 아닙니다.", "", ""), HttpStatus.BAD_REQUEST);
        }

        ResponseParcel responseParcel = createResponseParcel(user, qrInfo);
        log.info("ResponseParcel :{}", responseParcel);
//        ResponseQRcodeInto responseQRcodeInto = mapper.map(qrInfo, ResponseQRcodeInto.class);
//        responseQRcodeInto.setReceiverName(user.getName());
//        responseQRcodeInto.setPhoneNumber(user.getPhoneNumber());
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "회원이 주문한 상품이 배송 완료되었습니다.",  responseParcel, ""), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseData> clearPrivateInformation(String userId, String invoiceNo) {
        QRcode qrInfo = qRinfoRepository.findByInvoiceNo(invoiceNo);
        if(qrInfo == null) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.NOT_FOUND.getStatusCode(), "등록되지 않은 QR ID입니다. ", "", ""), HttpStatus.NOT_FOUND);
        }
        if(!qrInfo.getUserEntity().getUserId().equals(userId)){
            return new ResponseEntity<>(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "로그인 한 회원이 주문한 상품이 아닙니다.", "", ""), HttpStatus.BAD_REQUEST);
        }
        qRinfoRepository.delete(qrInfo);
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "회원이 주문한 상품의 정보 파기가 완료되었습니다.",  "", ""), HttpStatus.OK);
    }

    public ResponseEntity<ResponseData> assignQRId(String userId, RequestTemp requestTemp){
        UserEntity user = userRepository.findByUserId(userId);
        if(user == null) {
            return new ResponseEntity<>(new ResponseData(StatusEnum.NOT_FOUND.getStatusCode(), "등록되지 않은 회원ID입니다. ", "", ""), HttpStatus.NOT_FOUND);
        }
        QRcode info = mapper.map(requestTemp, QRcode.class);
        info.connectUser(user);
        user.addQRinfoList(info);

        qRinfoRepository.save(info);
        userRepository.save(user);
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "QR정보 저장이 완료되었습니다.",  requestTemp, ""), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseData> requestReturnParcel(String qrId) {
        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "반송 요청이 완료되었습니다.",  "", ""), HttpStatus.OK);
    }

    @Override
    public boolean isDuplicated(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
