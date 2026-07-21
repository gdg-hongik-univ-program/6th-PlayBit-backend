package com.playbit.backend.mission;

import com.playbit.backend.common.response.ApiResponse;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.room.dto.PlayingRoomDTO;
import com.playbit.backend.room.dto.RoomDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{entryCode}/missions")

public class MissionController {

    private final MissionService missionService;

    @PatchMapping("/{position}")
    public ResponseEntity<ApiResponse<MissionCompleteResponse>> completeMission(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @PathVariable String entryCode,
            @PathVariable Long position)
    {

        return ResponseEntity.ok().body(ApiResponse.success(missionService.completeMission(memberUuid, position, entryCode)));

    }

    @PatchMapping("/{position}/sabotaged")
    public ResponseEntity<ApiResponse<RoomDTO>> sabotageMission(
            @RequestHeader(value = "X-Member-Id") String memberUuid,
            @PathVariable String entryCode,
            @PathVariable Long position)
    {

        return ResponseEntity.ok()
                .body(ApiResponse.success(missionService.sabotageMission(memberUuid, position, entryCode)));

    }


}
