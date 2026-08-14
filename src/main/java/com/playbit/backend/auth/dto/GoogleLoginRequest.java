package com.playbit.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구글 로그인 요청 DTO")
public record GoogleLoginRequest(
        @Schema(description = "프론트엔드 구글 로그인 후 전달받은 ID Token", example = "eyJhbGciOiJSUzI1NiIs...")
        String idToken) {}