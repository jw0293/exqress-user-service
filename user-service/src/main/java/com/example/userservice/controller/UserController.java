package com.example.userservice.controller;

import com.example.userservice.StatusEnum;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserServiceImpl;
import com.example.userservice.vo.request.RequestToken;
import com.example.userservice.vo.response.Greeting;
import com.example.userservice.vo.request.RequestUser;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseUser;
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

    private final Greeting greeting;
    private final UserServiceImpl userService;
    private final Environment env;

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in User Service"
                + ", port(server.port) = " + env.getProperty("server.port")
                + ", token secret = " + env.getProperty("token.secret")
                + ", token expiration time = " + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    public String welcome(){
        log.error("Good!");
        String msg = greeting.getMessage();
        String dataSourceUrl = env.getProperty("spring.datasource.url");

        return msg + dataSourceUrl;
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseData> createUser(@RequestBody RequestUser user){
        if(userService.isDuplicated(user.getEmail())){
            return new ResponseEntity<>(new ResponseData(StatusEnum.EXISTED.getStatusCode(), "이미 존재하는 회원입니다.", ""), HttpStatus.CONFLICT);
        }
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return new ResponseEntity<>(new ResponseData(StatusEnum.OK.getStatusCode(), "회원가입 성공", responseUser), HttpStatus.OK);
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody RequestToken reissue) {
        return userService.reissue(reissue);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RequestToken logoutToken){
        return userService.logout(logoutToken);
    }

}
