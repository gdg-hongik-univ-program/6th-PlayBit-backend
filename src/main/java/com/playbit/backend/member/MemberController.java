package com.playbit.backend.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberDTO> createMember() {

        MemberDTO memberDTO = memberService.createMember();
        URI location = URI.create("/api/members/" + memberDTO.uuid().toString());

        return ResponseEntity.created(location).body(memberDTO);
    }
}
