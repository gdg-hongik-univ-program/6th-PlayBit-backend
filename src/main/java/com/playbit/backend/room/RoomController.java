package com.playbit.backend.room;

import com.playbit.backend.common.response.ApiResponse;
import com.playbit.backend.room.dto.CategoryRequest;
import com.playbit.backend.room.dto.SetRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor

public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{entryCode}")
    public Room enterRoom(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ){
        return roomService.enterRoom(entryCode, memberUuid) ;
    }

    @PostMapping
    public String createRoom(
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ){
        return roomService.createRoom();
    }

    @PatchMapping("/{entryCode}/category")
    public ResponseEntity<ApiResponse<SetRoomResponse>> setRoom(
            @PathVariable String entryCode,
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @RequestBody CategoryRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(roomService
                .setRoom(entryCode, memberUuid, request.category())));
    }
}
