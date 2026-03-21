package com.example.demo.appuser.controller;

import com.example.demo.appuser.domain.AppUser;
import com.example.demo.appuser.dto.AppUserDto;
import com.example.demo.appuser.service.AppUserService;
import com.example.demo.common.response.ApiResult;
import com.example.demo.common.response.ErrorResponse;
import com.example.demo.common.swagger.ApiCommonResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService userService;

    @Operation(
            summary = "사용자 상세 조회",
            description = "사용자 ID를 기반으로 상세 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppUser.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음"),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/jpa/{id}")
    public ApiResult<AppUser> getByJpa(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResult.success(userService.findByJpa(id));
    }

    @Operation(
            summary = "사용자 상세 조회",
            description = "사용자 ID를 기반으로 상세 정보를 조회한다."
    )
    @ApiCommonResponses // Swagger 공통 응답 자동 등록 커스텀 어노테이션 사용
    @GetMapping("/mybatis/{id}")
    public ApiResult<AppUserDto> getByMyBatis(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long id)
    {
        return ApiResult.success(userService.findByMyBatis(id));
    }
}