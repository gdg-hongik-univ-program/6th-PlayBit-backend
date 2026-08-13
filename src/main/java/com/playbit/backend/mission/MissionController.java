package com.playbit.backend.mission;

import com.playbit.backend.auth.LoginMember;
import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.member.Member;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms/{entryCode}/missions")
@Tag(name = "Mission API", description = "미션 관련 API입니다.")
public class MissionController {

    private final MissionService missionService;

    @PatchMapping(value = "/{position}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "미션 완료 및 사진 인증", description = "사용자가 미션 완료시 사진을 업로드하고 미션과 방의 상태를 업데이트합니다.")
    public ResponseEntity<ApiResponse<MissionCompleteResponse>> completeMission(
            @LoginMember Member member,
            @PathVariable String entryCode,
            @PathVariable Long position,
            @Parameter(description = "업로드할 미션 인증 사진 파일 (최대 10MB)", required = true)
            @RequestPart(value = "image") MultipartFile image,
            @RequestPart(value = "comment", required = false) String comment) {

        return ResponseEntity.ok().body(ApiResponse.success(
                missionService.completeMission(member, position, entryCode, image, comment)));
    }

    @PatchMapping(value = "/{position}/sabotage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사보타주 및 사진 인증", description = "상대방의 완료된 미션에 사보타주 사진을 업로드하여 상대방의 제한시간을 6시간 감소시킵니다.")
    public ResponseEntity<ApiResponse<MissionSabotageResponse>> sabotageMission(
            @LoginMember Member member,
            @PathVariable String entryCode,
            @PathVariable Long position,
            @Parameter(description = "업로드할 사보타주 인증 사진 파일 (최대 10MB)", required = true)
            @RequestPart(value = "image") MultipartFile image,
            @RequestPart(value = "comment", required = false) String comment) {

        return ResponseEntity.ok().body(ApiResponse.success(
                missionService.sabotageMission(member, position, entryCode, image, comment)));
    }
}