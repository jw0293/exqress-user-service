package com.example.userservice.controller;

import com.example.userservice.StatusEnum;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.TokenServiceImpl;
import com.example.userservice.service.UserServiceImpl;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.Greeting;
import com.example.userservice.vo.request.RequestUser;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class UserController {

    private final Environment env;
    private final Greeting greeting;
    private final UserServiceImpl userService;
    private final TokenServiceImpl tokenService;


    @PostMapping("/users")
    public ResponseEntity<ResponseData> createUser(@RequestBody RequestUser user){
        if(userService.isDuplicated(user.getEmail())){
            return new ResponseEntity<>(new ResponseData(StatusEnum.EXISTED.getStatusCode(), "이미 존재하는 회원입니다.", "", ""), HttpStatus.CONFLICT);
        }
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        /**
         * kafka로 Admin으로 전송해줘야함
         */

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "회원가입 성공", responseUser, ""), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestLogin login, HttpServletRequest request, HttpServletResponse response){
        ResponseEntity<ResponseData> responseData = userService.login(request, response, login);

        return responseData;
    }


    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        return tokenService.reissue(request, response);
    }

    @PostMapping("/logouts")
    public ResponseEntity<?> logout(@RequestBody RequestToken logoutToken){
        log.info("Logout accessToken : {}", logoutToken.getAccessToken());
        log.info("Logout refreshToken : {}", logoutToken.getRefreshToken());

        return tokenService.logout(logoutToken);
    }

}
