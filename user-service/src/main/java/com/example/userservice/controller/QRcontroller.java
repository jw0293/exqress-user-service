package com.example.userservice.controller;

import com.example.userservice.service.QRcodeService;
import com.example.userservice.service.UserServiceImpl;
import com.example.userservice.vo.request.RequestQRcode;
import com.example.userservice.vo.request.RequestTemp;
import com.example.userservice.vo.response.ResponseData;
import com.example.userservice.vo.response.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "QR 컨트롤러", description = "QR코드 API입니다.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class QRcontroller {

    private final UserServiceImpl userService;
    private final QRcodeService qRcodeService;

    @Operation(summary = "QR 스캔", description = "사용자가 배송된 물품의 QR코드를 스캔합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스캔 성공", content = @Content(schema = @Schema(implementation = ResponseData.class))),
            @ApiResponse(responseCode = "401", description = "인가 기능이 확인되지 않은 접근", content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/scan")
    public ResponseEntity<ResponseData> scanQR(HttpServletRequest request, @RequestBody RequestQRcode qrCode){
        String userId = userService.getUserIdThroughRequest(request);
        return qRcodeService.scanQRcode(userId, qrCode);
    }
}
