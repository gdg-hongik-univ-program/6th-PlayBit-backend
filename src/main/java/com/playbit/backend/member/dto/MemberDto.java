package com.playbit.backend.member.dto;

import com.playbit.backend.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 응답 DTO")
public record MemberDto(
        @Schema(description = "회원 고유 ID", example = "1") Long memberId,
        @Schema(description = "회원 이메일", example = "user@gmail.com") String email,
        @Schema(description = "회원 닉네임", example = "플레이비트123") String nickname) {

    public static MemberDto from(Member member) {
        return new MemberDto(member.getMemberId(), member.getEmail(), member.getNickname());
    }
}