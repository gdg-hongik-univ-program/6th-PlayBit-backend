package com.playbit.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 설정 요청 DTO")
public record SetNicknameRequest(
        @Schema(description = "설정할 닉네임", example = "player_one") String nickname) {}
