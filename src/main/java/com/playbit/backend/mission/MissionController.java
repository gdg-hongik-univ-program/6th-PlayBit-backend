package com.playbit.backend.mission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{entryCode}/missions")

public class MissionController {

    private final MissionService missionService;

    @PatchMapping("/{position}")
    public ResponseEntity<?> completeMission(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @PathVariable String entryCode,
            @PathVariable Long position)
    {

        missionService.completeMission(memberUuid, position, entryCode);


        return ResponseEntity.ok().build();

    }

    @PatchMapping("/{position}/sabotaged")
    public ResponseEntity<?> sabotageMission(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @PathVariable String entryCode,
            @PathVariable Long position)
    {

        missionService.sabotageMission(memberUuid, position, entryCode);


        return ResponseEntity.ok().build();

    }


}
