package com.playbit.backend.member;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberDTO>> createMember() {

        MemberDTO memberDTO = memberService.createMember();
        URI location = URI.create("/api/members/" + memberDTO.uuid().toString());

        return ResponseEntity.created(location).body(ApiResponse.success(memberDTO));
    }
}
