package com.playbit.backend.player;

import com.playbit.backend.auth.LoginMember;
import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.Member;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Player API", description = "플레이어 관련 API입니다.")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/{entryCode}/players")
    @Operation(summary = "방 참가자 등록", description = "사용자를 방에 등록시킵니다.")
    public ResponseEntity<ApiResponse<PlayerJoinResponse>> registerPlayer(
            @PathVariable("entryCode") String entryCode,
            @LoginMember Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                playerService.registerPlayer(entryCode, member)));
    }
}