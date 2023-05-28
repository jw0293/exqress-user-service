package com.example.userservice;

import com.example.userservice.entity.QRcode;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.QRcodeRepository;
import com.example.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInit {
    private final QRcodeRepository qRcodeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    @PostConstruct
    @Transactional
    public void initData(){
        UserEntity user1 = new UserEntity("k12@gmail.com", "김재한", "1234", bCryptPasswordEncoder.encode("pwd"), "010-12-31", LocalDateTime.now());

        QRcode qRcode1 = new QRcode("egjh14813fghasd", "딜도", "2931753", "false", LocalDateTime.now());
        QRcode qRcode2 = new QRcode("1", "오나홀", "64141312", "false", LocalDateTime.now());

        userRepository.save(user1);

        user1.addQRinfoList(qRcode1);
        user1.addQRinfoList(qRcode2);
        userRepository.save(user1);

        qRcode1.setUserEntity(user1);
        qRcode2.setUserEntity(user1);
        qRcodeRepository.save(qRcode1);
        qRcodeRepository.save(qRcode2);

    }
}
