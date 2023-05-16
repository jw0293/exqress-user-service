package com.example.userservice.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CookieUtils {

    private final Environment env;

    public Cookie createCookie(String cookieName, String value){
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) Long.parseLong(env.getProperty("token.refresh_expiration_time")));
        cookie.setPath("/");

        return cookie;
    }

    public Cookie getCookie(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies==null) return null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(cookieName)){
                return cookie;
            }
        }
        return null;
    }
}
