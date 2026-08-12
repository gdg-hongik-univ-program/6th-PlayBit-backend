package com.playbit.backend.mission;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.playbit.backend.auth.MemberAuthInterceptor;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDTO;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.room.Category;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomStatus;
import com.playbit.backend.room.dto.FinishedRoomDTO;
import com.playbit.backend.room.dto.PlayingRoomDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MissionController.class)
public class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MissionService missionService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private MemberAuthInterceptor memberAuthInterceptor;

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 게임이 끝나지 않아 계속 진행된다.")
    void completeMissionTest_gameNotEnded() throws Exception {

        // given
        Member member = new Member(UUID.randomUUID().toString());
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;
        Room room = new Room(3L, RoomStatus.PLAYING, entryCode, null, Category.STUDY, 3L, 4L, null, null, false, null);

        // 🌟 11개의 필드 인자에 맞게 뒤에 null 2개 추가 (comment, sabotageComment)
        Mission mission = new Mission(
                8L,
                room,
                3L,
                null,
                member,
                LocalDateTime.now(),
                "https://test-image-url.jpg",
                false,
                null,
                "완료 코멘트",
                null);

        // 🌟 서비스 메서드 파라미터가 5개로 늘었으므로 any() 하나 추가
        given(missionService.completeMission(anyString(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionCompleteResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission)));

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);

        MockMultipartFile image =
                new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        // when&then
        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("X-Member-Id", member.getMemberUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data.mission.comment").value("완료 코멘트")) // 🌟
                .andExpect(jsonPath("$.data.room.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.room.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.room.currentTurnSabotaged").value(false))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.PLAYING.toString()));
    }

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 승패가 결정되어 게임이 끝났다.")
    void completeMissionTest_gameEnded_Not_Draw() throws Exception {

        // given
        Member member = new Member(UUID.randomUUID().toString());
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;
        Room room =
                new Room(90L, RoomStatus.FINISHED, entryCode, member, Category.STUDY, 5L, 4L, null, null, false, null);

        // 🌟 11개의 필드 인자에 맞게 뒤에 null 2개 추가
        Mission mission3 = new Mission(
                813L,
                room,
                3L,
                null,
                member,
                LocalDateTime.now(),
                "https://test-image-url.jpg",
                false,
                null,
                null,
                null);

        // 🌟 any() 하나 추가
        given(missionService.completeMission(anyString(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission3)));

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);

        MockMultipartFile image =
                new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        // when&then
        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("X-Member-Id", member.getMemberUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.data.room.winnerMemberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data.room.isDraw").value(false));
    }

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 게임이 무승부로 끝났다.")
    void completeMissionTest_gameEnded_draw() throws Exception {

        // given
        Member member = new Member(UUID.randomUUID().toString());
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;
        Room room =
                new Room(3L, RoomStatus.FINISHED, entryCode, null, Category.STUDY, 3L, 10L, null, null, false, true);

        // 🌟 11개의 필드 인자에 맞게 뒤에 null 2개 추가
        Mission mission = new Mission(
                8L, room, 3L, null, member, LocalDateTime.now(), "https://test-image-url.jpg", false, null, null, null);

        // 🌟 any() 하나 추가
        given(missionService.completeMission(anyString(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission)));

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);

        MockMultipartFile image =
                new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        // when&then
        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("X-Member-Id", member.getMemberUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(member.getMemberId()))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.data.room.winnerMemberId").value(nullValue()))
                .andExpect(jsonPath("$.data.room.isDraw").value(true));
    }

    @Test
    @DisplayName("올바른 사용자가 사보타주 요청을 보냈고, 상대방의 제한 시간이 6시간 감소하였다.")
    void sabotageMission_success() throws Exception {

        // given
        Member member = new Member(UUID.randomUUID().toString());
        Member opponent = new Member(5L, UUID.randomUUID().toString());
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;

        LocalDateTime completedAt = LocalDateTime.now();

        Room room = new Room(
                12L,
                RoomStatus.PLAYING,
                entryCode,
                null,
                Category.STUDY,
                3L,
                4L,
                completedAt,
                completedAt.minusHours(6L),
                true,
                null);

        Mission mission = new Mission();
        mission.setPosition(position);
        mission.setCompletedBy(opponent);
        mission.setCompletedAt(completedAt);
        mission.setImageUrl("https://test-mission-image.jpg");
        mission.setSabotagedByOpponent(true);
        mission.setSabotageImageUrl("https://test-sabotage-image.jpg");
        mission.setSabotageComment("사보타주 코멘트"); // 🌟

        // 🌟 파라미터가 5개로 늘었으므로 any() 하나 추가
        given(missionService.sabotageMission(anyString(), anyLong(), anyString(), any(), any()))
                .willReturn(new MissionSabotageResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission)));

        given(memberAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);

        MockMultipartFile image = new MockMultipartFile(
                "image", "sabotage.jpg", MediaType.IMAGE_JPEG_VALUE, "sabotage-image-content".getBytes());

        // when&then
        mockMvc.perform(multipart("/api/rooms/{entryCode}/missions/{position}/sabotage", entryCode, 3L)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header("X-Member-Id", member.getMemberUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.room.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.room.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.room.currentTurnSabotaged").value(true))
                .andExpect(jsonPath("$.data.room.turnStartedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.room.turnDeadline")
                        .value(completedAt
                                .minusHours(6L)
                                .truncatedTo(ChronoUnit.MICROS)
                                .format(MICROSECONDS_FMT)))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.PLAYING.toString()))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(opponent.getMemberId()))
                .andExpect(jsonPath("$.data.mission.sabotagedByOpponent").value(true))
                .andExpect(jsonPath("$.data.mission.sabotageImageUrl").value("https://test-sabotage-image.jpg"))
                .andExpect(jsonPath("$.data.mission.sabotageComment").value("사보타주 코멘트")); // 🌟
    }

    private static final DateTimeFormatter MICROSECONDS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
}
