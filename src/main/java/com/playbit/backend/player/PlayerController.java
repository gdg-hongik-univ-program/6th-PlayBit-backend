package com.playbit.backend.player;

import com.playbit.backend.player.dto.PlayerJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor

public class PlayerController {
    private final PlayerService playerService;

    //방 참가자 등록 요청
    @PostMapping
    public Player registerPlayer(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @RequestBody PlayerJoinRequest request
    ){
        return playerService.registerPlayer(request.entryCode(), memberUuid);
    }

}
