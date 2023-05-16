package com.example.deliveryservice.security;

import com.example.deliveryservice.StatusEnum;
import com.example.deliveryservice.constants.AuthConstants;
import com.example.deliveryservice.utils.CookieUtils;
import com.example.deliveryservice.utils.TokenUtils;
import com.example.deliveryservice.vo.response.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final TokenUtils tokenUtils;
    private final CookieUtils cookieUtils;
    private static final String ERROR_MESSAGE = "유효하지 않은 JWT입니다.";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String header = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if(header == null) {
            response.setStatus(401);
            response.getWriter().write(mapper.writeValueAsString(new ResponseData(StatusEnum.Unauthorized.getStatusCode(), ERROR_MESSAGE, "", "")));
            return false;
        }

        if(StringUtils.hasText(header) && header.startsWith(AuthConstants.TOKEN_TYPE)){
            String bearerToken = header.substring(7);
            if(tokenUtils.isValidToken(bearerToken)) return true;
        }

        Cookie refreshCookie = cookieUtils.getCookie(request, AuthConstants.REFRESH_HEADER);
        if(refreshCookie == null) {
            response.setStatus(401);
            response.getWriter().write(mapper.writeValueAsString(new ResponseData(StatusEnum.Unauthorized.getStatusCode(), ERROR_MESSAGE, "", "")));
            return false;
        }
        if(tokenUtils.isValidToken(refreshCookie.getValue())) {
            response.setStatus(400);
            response.sendRedirect("/error/unauthorized");
            response.getWriter().write(mapper.writeValueAsString(new ResponseData(StatusEnum.BAD_REQUEST.getStatusCode(), "토큰 재발급 요청", "", "")));
            return false;
        }

        response.setStatus(401);
        response.getWriter().write(mapper.writeValueAsString(new ResponseData(StatusEnum.Unauthorized.getStatusCode(), ERROR_MESSAGE, "", "")));
        return false;
    }

}
