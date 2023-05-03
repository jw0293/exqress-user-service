package com.example.userservice.security;


import com.example.userservice.service.UserService;
import com.example.userservice.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        final String email = token.getName();
        final String password = (String) token.getCredentials();

        log.info("Provider!");
        log.info("Email : {}", token.getName());
        log.info("Pwd : {}", token.getCredentials());

        UserDetails userDetails = userService.loadUserByUsername(email);

        log.info("UserDetails Email : {}", userDetails.getUsername());
        log.info("UserDetails Pwd : {}", userDetails.getPassword());

        if(!bCryptPasswordEncoder.matches(password, userDetails.getPassword())){
            log.info("FAIL!!!!");
            throw new BadCredentialsException(userDetails.getUsername() + "Invalid password");
        }

        log.info("Success!!");

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
