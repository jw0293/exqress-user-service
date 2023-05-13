package com.example.userservice.security;

import com.example.userservice.StatusEnum;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.dto.TokenInfo;
import com.example.userservice.utils.TokenUtils;
import com.example.userservice.vo.request.RequestLogin;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserService userService;
    private final TokenUtils tokenUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            RequestLogin creeds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            log.info("Email : {}", creeds.getEmail());
            log.info("Password : {}", creeds.getPassword());

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                        creeds.getEmail(),
                        creeds.getPassword(),
                        new ArrayList<>()
            ));

        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        ObjectMapper mapper = new ObjectMapper();
        String userName = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDetails = userService.getUserDetailsByEmail(userName);

        TokenInfo tokenInfo = tokenUtils.generateToken(userDetails.getUserId());

        redisTemplate.opsForValue()
                .set("RT:" + userDetails.getUserId(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        log.info("AccessToken : {}", tokenInfo.getAccessToken());
        log.info("RefreshToken : {}", tokenInfo.getRefreshToken());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("refreshToken", tokenInfo.getRefreshToken());
        response.getWriter().write(mapper.writeValueAsString(new ResponseData(StatusEnum.OK.getStatusCode(), "로그인 성공", new ResponseLogin(userDetails.getUserId(), tokenInfo.getRefreshTokenExpirationTime()))));
    }
}
