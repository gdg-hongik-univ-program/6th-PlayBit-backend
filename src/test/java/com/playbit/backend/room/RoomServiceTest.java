package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.Content;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.dto.EnterRoomResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private MemberRepository memberRepository;
    @Mock
    private MissionRepository missionRepository;


    @Nested
    @DisplayName("방 입장 테스트")
    class EnterRoomTest{

        @Test
        @DisplayName("정상적인 방 입장 요청 시 정상적인 DTO가 반환된다.")
        void enterRoom_success(){
            // given
            String entryCode = "ABC123";
            String memberUuid = "uuid-member-1";

            // 1. 테스트용 엔티티 생성
            Member cuurentMember = new Member(1L,memberUuid);
            Member oppentMember = new Member(2L, "uuid-member-2");

            // 정상 진행중인 방
            Room room = new Room(1L, RoomStatus.PLAYING, entryCode, null, Category.STUDY,
                    1L, null, LocalDateTime.now(), LocalDateTime.now().plusHours(24),
                    null, null);

            Player player1 = new Player(room,cuurentMember, PlayerRole.O);
            Player player2 = new Player(room,oppentMember,PlayerRole.X);

            Mission mission = new Mission(
                    1L, room, 1L, Content.STUDY_1, cuurentMember, LocalDateTime.now()
            );

            // 2. 가짜 객체 행동 정의
            given(roomRepository.findByEntryCode(entryCode)).willReturn(Optional.of(room));
            given(memberRepository.findByMemberUuid(memberUuid)).willReturn(Optional.of(cuurentMember));
            given(playerRepository.findByRoom(room)).willReturn(List.of(player1, player2));
            given(missionRepository.findByRoom(room)).willReturn(List.of(mission));

            // when
            EnterRoomResponse response = roomService.enterRoom(entryCode,memberUuid);

            // then
            assertThat(response).isNotNull();
            assertThat(response.entryCode()).isEqualTo(entryCode);
            assertThat(response.status()).isEqualTo(RoomStatus.PLAYING);
            assertThat(response.category()).isEqualTo(Category.STUDY);
            assertThat(response.myMemberId()).isEqualTo(1L);
            assertThat(response.currentTurnMemberId()).isEqualTo(1L);

            // Mission DTO 검증
            assertThat(response.missions()).hasSize(1);
            assertThat(response.missions().get(0).completedByMemberId()).isEqualTo(1L);

            // Player DTO 검증
            assertThat(response.players()).hasSize(2);
            assertThat(response.players())
                    .extracting("memberId")
                    .containsExactlyInAnyOrder(1L,2L);

            // Repository 호출 횟수 검증
            verify(roomRepository, times(1)).findByEntryCode(entryCode);
            verify(memberRepository, times(1)).findByMemberUuid(memberUuid);
            verify(playerRepository, times(1)).findByRoom(room);
            verify(missionRepository, times(1)).findByRoom(room);
        }
    }
}
