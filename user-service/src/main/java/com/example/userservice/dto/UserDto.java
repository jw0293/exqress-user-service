package com.example.userservice.dto;

import com.example.userservice.vo.ResponseItem;
import lombok.Data;
import org.springframework.dao.DataAccessException;

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
