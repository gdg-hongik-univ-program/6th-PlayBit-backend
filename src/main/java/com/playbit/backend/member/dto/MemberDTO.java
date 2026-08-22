package com.playbit.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "회원 정보 응답 DTO")
public record MemberDTO (
        @Schema(description = "회원 UUID", example = "3cf20bbb-fa6a-4487-80a4-ac8ff0a97881") UUID uuid
) {}
