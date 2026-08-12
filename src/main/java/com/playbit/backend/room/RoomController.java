package com.playbit.backend.room;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.room.dto.EnterRoomResponse;
import com.playbit.backend.room.dto.RoomCreateResponse;
import com.playbit.backend.room.dto.SetRoomRequest;
import com.playbit.backend.room.dto.SetRoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Room API", description = "방 관련 API입니다.")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 조회", description = "게임 화면에 필요한 정보를 업데이트하고 로드합니다.")
    @GetMapping("/{entryCode}")
    public ResponseEntity<ApiResponse<EnterRoomResponse>> enterRoom(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid) {
        return ResponseEntity.ok(ApiResponse.success(roomService.enterRoom(entryCode, memberUuid)));
    }

    @Operation(summary = "빈 방 생성", description = "임시 방을 생성하고 입장 코드를 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomCreateResponse>> createRoom(
            @RequestHeader(value = "X-Member-Id") String memberUuid) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roomService.createRoom()));
    }

    @Operation(summary = "카테고리 선택, 방 이름 지정", description = "사용자가 선택한 카테고리를 반영하고 방 이름을 설정합니다.")
    @PatchMapping("/{entryCode}/category")
    public ResponseEntity<ApiResponse<SetRoomResponse>> setRoom(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @RequestBody SetRoomRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        roomService.setRoom(
                                entryCode, memberUuid, request.category(), request.roomName())));
    }
}
