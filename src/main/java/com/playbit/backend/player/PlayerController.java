package com.playbit.backend.player;

import com.playbit.backend.common.response.ApiResponse;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor

public class PlayerController {
    private final PlayerService playerService;

    //방 참가자 등록 요청
    @PostMapping("/{entryCode}/players")
    public ResponseEntity<ApiResponse<PlayerJoinResponse>> registerPlayer(
            @PathVariable ("entryCode") String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ){
        return ResponseEntity.ok(ApiResponse.success(
                playerService.registerPlayer(entryCode, memberUuid)
        ));
    }

}
