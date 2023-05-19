package com.example.userservice.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class RequestUser {

    @NotNull(message = "Email cannot be null")
    @Size(min = 2, message = "Email not be less than two characters")
    @Email
    @Schema(description = "사용자 이메일", nullable = false, example = "hshin@dgu.ac.kr")
    private String email;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, message = "Name not be less than two characters")
    @Schema(description = "사용자 이름", nullable = false, example = "홍길동")
    private String name;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password not be less than two characters")
    @Schema(description = "사용자 비밀번호", nullable = false, example = "22e2412%^dadh")
    private String password;

    @NotNull(message = "PhoneNumber cannot be null")
    @Schema(description = "사용자 핸드폰 번호", nullable = false, example = "010-1234-4321")
    private String phoneNumber;


}
