package com.playbit.backend.room;

import com.playbit.backend.common.response.ApiResponse;
import com.playbit.backend.room.dto.CategoryRequest;
import com.playbit.backend.room.dto.SetRoomResponse;
import com.playbit.backend.room.dto.RoomCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public ResponseEntity<RoomCreateResponse> createRoom(
            @RequestHeader(value = "X-Member-Id") String memberUuid
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom());
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
