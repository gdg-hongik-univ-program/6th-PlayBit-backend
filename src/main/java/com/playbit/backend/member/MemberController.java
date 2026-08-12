package com.playbit.backend.member;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.dto.MemberStatsDTO;
import com.playbit.backend.member.dto.MemberDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.playbit.backend.member.dto.SetNicknameRequest;
import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "사용자 관련 API입니다.")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "사용자 등록", description = "처음 접속하는 사용자이면 UUID를 부여하고 등록시킵니다.")
    public ResponseEntity<ApiResponse<MemberDTO>> createMember() {

        MemberDTO memberDTO = memberService.createMember();
        URI location = URI.create("/api/members/" + memberDTO.uuid().toString());

        return ResponseEntity.created(location).body(ApiResponse.success(memberDTO));
    }

    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "헤더 X-Member-Id 로 회원 UUID 를 받아 닉네임을 설정합니다.")
    public ResponseEntity<ApiResponse<Void>> setMemberNickname(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @RequestBody SetNicknameRequest request) {
        memberService.setMemberNickname(memberUuid, request.nickname());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/stats")
    @Operation(summary = "특정 회원 미션 통계 조회", description = "헤더 X-Member-Id 로 회원 UUID 를 받아 해당 회원의 총 성공 미션 수와 연속 스트릭을 조회합니다.")
    public ResponseEntity<ApiResponse<MemberStatsDTO>> getMemberStats(
            @RequestHeader(value = "X-Member-Id") String memberUuid) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberStats(memberUuid)));
    }
}
