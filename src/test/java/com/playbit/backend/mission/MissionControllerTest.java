package com.playbit.backend.mission;

import com.playbit.backend.config.MemberAuthInterceptor;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDTO;
import com.playbit.backend.room.Category;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomStatus;
import com.playbit.backend.room.dto.FinishedRoomDTO;
import com.playbit.backend.room.dto.PlayingRoomDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

        //given
        Member member = new Member(UUID.randomUUID().toString()); // 이 친구의 memberId는 5L이라고 가정
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;
        Room room = new Room(3L, RoomStatus.PLAYING, entryCode, null, Category.STUDY, 3L, 4L, null, null, false);
        Mission mission = new Mission(8L, room, 3L, null, member, LocalDateTime.now());

        given(missionService.completeMission(member.getMemberUuid(), position, entryCode))
                .willReturn(new MissionCompleteResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission)));

        given(memberAuthInterceptor.preHandle(any(), any(), any()))
                .willReturn(true);

        //when&then
        mockMvc.perform(patch("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                .header("X-Member-Id", member.getMemberUuid())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(member.getMemberId()))
                //.andExpect(jsonPath("$.data.mission.completedAt").value(LocalDateTime.now()))
                .andExpect(jsonPath("$.data.room.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.room.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.room.currentTurnSabotaged").value(false))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.PLAYING.toString()));
    }

    @Test
    @DisplayName("올바른 사용자가 미션 완료 요청을 보냈고, 게임이 끝났다.")
    void completeMissionTest_gameEnded() throws Exception {

        //given
        Member member = new Member(UUID.randomUUID().toString()); // 이 친구의 memberId는 5L이라고 가정
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;
        Room room = new Room(3L, RoomStatus.FINISHED, entryCode, member, Category.STUDY, 3L, 4L, null, null, false);
        Mission mission = new Mission(8L, room, 3L, null, member, LocalDateTime.now());

        given(missionService.completeMission(member.getMemberUuid(), position, entryCode))
                .willReturn(new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission)));

        given(memberAuthInterceptor.preHandle(any(), any(), any()))
                .willReturn(true);

        //when&then
        mockMvc.perform(patch("/api/rooms/{entryCode}/missions/{position}", entryCode, 3L)
                        .header("X-Member-Id", member.getMemberUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mission.position").value(3L))
                .andExpect(jsonPath("$.data.mission.completedByMemberId").value(member.getMemberId()))
                //.andExpect(jsonPath("$.data.mission.completedAt").value(LocalDateTime.now()))
                .andExpect(jsonPath("$.data.room.status").value(RoomStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.data.room.winnerMemberId").value(member.getMemberId()));
    }

    @Test
    @DisplayName("올바른 사용자가 사보타주 요청을 보냈고, 상대방의 제한 시간이 6시간 감소하였다..")
    void sabotageMission_success() throws Exception {

        //given
        Member member = new Member(UUID.randomUUID().toString()); // 이 친구의 memberId는 5L이라고 가정
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long position = 3L;

        LocalDateTime completedAt = LocalDateTime.now();

        Room room = new Room(12L, RoomStatus.PLAYING, entryCode, null, Category.STUDY, 3L, 4L, completedAt, completedAt.minusHours(6L), true);

        given(missionService.sabotageMission(member.getMemberUuid(), position, entryCode))
                .willReturn(PlayingRoomDTO.from(room));

        given(memberAuthInterceptor.preHandle(any(), any(), any()))
                .willReturn(true);

        //when&then
        mockMvc.perform(patch("/api/rooms/{entryCode}/missions/{position}/sabotaged", entryCode, 3L)
                        .header("X-Member-Id", member.getMemberUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentTurnMemberId").value(3L))
                .andExpect(jsonPath("$.data.currentTurnNumber").value(4L))
                .andExpect(jsonPath("$.data.currentTurnSabotaged").value(true))
                .andExpect(jsonPath("$.data.turnStartedAt").value(completedAt.toString()))
                .andExpect(jsonPath("$.data.turnDeadline").value(completedAt.minusHours(6L).toString()))
                .andExpect(jsonPath("$.data.status").value(RoomStatus.PLAYING.toString()));
    }
}
