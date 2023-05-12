package com.example.userservice.dto;

import com.example.userservice.vo.response.ResponseItem;
import lombok.Data;

import java.util.*;

@Data
public class UserDto {

    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String encryptedPwd;
    private List<ResponseItem> items;
}
