package com.example.userservice.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResponseUser {

    @Schema(description = "사용자 이메일", nullable = false, example = "junenb@naver.com")
    private String email;
    @Schema(description = "사용자 이름", nullable = false, example = "전두환")
    private String name;
    @Schema(description = "사용자 ID", nullable = false, example = "yuaytb2429f")
    private String userId;
}
