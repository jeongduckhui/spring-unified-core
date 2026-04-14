package com.example.demo.sample.swagger.controller;

import com.example.demo.common.response.ApiResult;
import com.example.demo.sample.swagger.dto.UserResponse;
import com.example.demo.sample.swagger.dto.UserSearchRequest;
import com.example.demo.sample.swagger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tags(value = {
        @Tag(name = "User API", description = "사용자 관련 API")
})
public class UserController {

    private final UserService service;

    @Operation(
            summary = "사용자 조회",
            description = "사용자 목록을 조회한다.\n\n" +
                    "- userId: 정확 조회\n" +
                    "- userName: 부분 검색\n" +
                    "- deptName: 정확 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping("/api/users")
    public ApiResult<List<UserResponse>> search(
            @ParameterObject
            @Parameter(description = "조회 조건 DTO")
            UserSearchRequest request
    ) {

        List<UserResponse> data = service.search(request);

        return ApiResult.success(data, "SUCCESS", "사용자 조회 성공");
    }
}