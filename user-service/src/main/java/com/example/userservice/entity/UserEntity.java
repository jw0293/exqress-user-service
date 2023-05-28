package com.example.userservice.entity;

import com.example.userservice.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table
public class UserEntity extends BaseTimeEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_entity_id")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String encryptedPwd;

    @Column(nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "userEntity")
    private List<QRcode> qRinfoList = new ArrayList<>();

    public UserEntity(String email, String name, String userId, String password, String phoneNumber, LocalDateTime cur) {
        this.email = email;
        this.name = name;
        this.userId = userId;
        this.encryptedPwd = password;
        this.phoneNumber = phoneNumber;
        this.createdAt = cur;
    }

    public UserEntity(){

    }

    public void addQRinfoList(QRcode qRinfo){
        this.qRinfoList.add(qRinfo);
    }
}
