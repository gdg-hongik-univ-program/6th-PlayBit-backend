package com.playbit.backend.room;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
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
}
