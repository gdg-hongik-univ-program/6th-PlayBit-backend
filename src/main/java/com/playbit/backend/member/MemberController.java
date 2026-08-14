package com.playbit.backend.member;

import com.playbit.backend.auth.LoginMember;
import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.dto.GetStatsResponse;
import com.playbit.backend.member.dto.SetNicknameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member API", description = "사용자 관련 API입니다.")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "닉네임 수정", description = "로그인된 회원의 닉네임을 설정합니다.")
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> setMemberNickname(
            @LoginMember Member member, @RequestBody SetNicknameRequest request) {
        memberService.setMemberNickname(member, request.nickname());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "특정 회원 미션 통계 조회", description = "로그인된 회원의 총 성공 미션 수와 연속 스트릭을 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<GetStatsResponse>> getMemberStats(
            @LoginMember Member member) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberStats(member)));
    }
}