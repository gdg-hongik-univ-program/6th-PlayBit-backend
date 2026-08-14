package com.playbit.backend.room;

import com.playbit.backend.auth.LoginMember;
import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.Member;
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

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "방 관련 API입니다.")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{entryCode}")
    @Operation(summary = "방 조회", description = "게임 화면에 필요한 정보를 업데이트하고 로드합니다.")
    public ResponseEntity<ApiResponse<EnterRoomResponse>> getRoomInfo(
            @PathVariable String entryCode,
            @LoginMember Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(roomService.getRoomInfo(entryCode, member)));
    }

    @PostMapping
    @Operation(summary = "빈 방 생성", description = "임시 방을 생성하고 입장 코드를 반환합니다.")
    public ResponseEntity<ApiResponse<RoomCreateResponse>> createRoom(
            @LoginMember Member member
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roomService.createRoom(member)));
    }

    @PatchMapping("/{entryCode}/category")
    @Operation(summary = "카테고리 및 방 이름 선택", description = "사용자가 선택한 카테고리와 방 이름을 반영합니다.")
    public ResponseEntity<ApiResponse<SetRoomResponse>> setRoom(
            @PathVariable String entryCode,
            @LoginMember Member member,
            @RequestBody SetRoomRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                roomService.setRoom(entryCode, member, request.category(), request.roomName())));
    }
}