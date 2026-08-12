package com.playbit.backend.player;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.player.dto.GetRoomResponse;
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
            @PathVariable ("entryCode") String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ){
        return ResponseEntity.ok(ApiResponse.success(
                playerService.registerPlayer(entryCode, memberUuid)
        ));
    }

    @GetMapping
    @Operation(summary = "방 목록 조회", description = "사용자가 입장한 모든 방을 조회합니다.")
    public ResponseEntity<ApiResponse<GetRoomResponse>> getRooms(
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                playerService.getRooms(memberUuid)
        ));
    }
    
    @DeleteMapping("/{entryCode}/players")
    @Operation(summary = "방 탈퇴", description = "현재 사용자가 방을 탈퇴합니다. 두 명 다 탈퇴하면 방 자체가 삭제됩니다.")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid) {
        playerService.leaveRoom(entryCode, memberUuid);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
