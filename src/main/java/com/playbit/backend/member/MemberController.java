package com.playbit.backend.member;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.dto.MemberDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
