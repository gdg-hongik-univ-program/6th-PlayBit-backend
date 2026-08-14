package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import com.playbit.backend.mission.Content;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.mission.MissionService;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.dto.EnterRoomResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private MissionService missionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("방 정보 조회 테스트")
    class GetRoomInfoTest {

        @Test
        @DisplayName("정상적인 방 정보 조회 요청 시 정상적인 DTO가 반환된다.")
        void getRoomInfo_success() {
            String entryCode = "ABC123";

            Member currentMember = Member.builder().memberId(1L).googleSub("sub-1").build();
            Member opponentMember = Member.builder().memberId(2L).googleSub("sub-2").build();

            Room room = new Room(
                    1L,
                    RoomStatus.PLAYING,
                    entryCode,
                    null,
                    Category.STUDY,
                    "테스트방",
                    1L,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(24),
                    null,
                    null);

            Player player1 = new Player(room, currentMember, PlayerRole.O);
            Player player2 = new Player(room, opponentMember, PlayerRole.X);

            // 🌟 11개의 생성자 인자 규격에 맞춰 뒤에 null 2개를 추가합니다. (comment, sabotageComment)
            Mission mission = new Mission(
                    1L, room, 1L, Content.STUDY_1, currentMember, LocalDateTime.now(), null, false, null, null, null);

            given(roomRepository.findByEntryCode(entryCode)).willReturn(Optional.of(room));
            given(playerRepository.findByRoom(room)).willReturn(List.of(player1, player2));
            given(missionRepository.findByRoom(room)).willReturn(List.of(mission));

            EnterRoomResponse response = roomService.getRoomInfo(entryCode, currentMember);

            assertThat(response).isNotNull();
            assertThat(response.entryCode()).isEqualTo(entryCode);
            assertThat(response.status()).isEqualTo(RoomStatus.PLAYING);
            assertThat(response.category()).isEqualTo(Category.STUDY);
            assertThat(response.myMemberId()).isEqualTo(1L);
            assertThat(response.currentTurnMemberId()).isEqualTo(1L);

            assertThat(response.missions()).hasSize(1);
            assertThat(response.missions().get(0).completedByMemberId()).isEqualTo(1L);

            assertThat(response.players()).hasSize(2);
            assertThat(response.players()).extracting("memberId").containsExactlyInAnyOrder(1L, 2L);

            verify(roomRepository, times(1)).findByEntryCode(entryCode);
            verify(playerRepository, times(1)).findByRoom(room);
            verify(missionRepository, times(1)).findByRoom(room);
        }
    }
}