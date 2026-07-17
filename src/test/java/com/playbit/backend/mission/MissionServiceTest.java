package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MissionServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MissionService missionService;

    @Test
    @DisplayName("존재하지 않는 유저가 미션 완료를 누르면 UserNotFoundException 발생")
    void completeMission_userNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.empty());

        //when & then

        assertThatThrownBy(()->missionService.completeMission(memberUuid, position, roomCode))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("존재하지 않는 멤버입니다.");

        verify(roomRepository, never()).findByEntryCode(anyString());
        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 방으로 미션 완료 요청이 들어오면 RoomNotFoundException이 발생")
    void completeMission_roomNotFound() {
        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(new Member()));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(()->missionService.completeMission(memberUuid, position, roomCode))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("존재하지 않는 방입니다.");

        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
        verify(playerRepository, never()).findByRoomAndMemberNot(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 미션으로 미션 완료 요청이 들어오면 MissionNotFoundException이 발생")
    void completeMission_missionNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(new Member()));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(new Room()));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(()->missionService.completeMission(memberUuid, position, roomCode))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("존재하지 않는 미션입니다.");

        verify(playerRepository, never()).findByRoomAndMemberNot(any(), any());
    }

    @Test
    @DisplayName("미션 완료 요청이 들어왔는데 상대방이 없으면 OpponentNotFoundException이 발생")
    void completeMission_playerNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member =  new Member(1L, memberUuid);
        Room room = new Room(1L, null, null, null, null, 1L, null, null, null, null);
        Mission mission = new Mission();

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(()->missionService.completeMission(memberUuid, position, roomCode))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("존재하지 않는 플레이어입니다.");

        assertThat(mission.getCompletedBy()).isNull();
        assertThat(mission.getCompletedAt()).isNull();

    }

    @Test
    @DisplayName("미션 완료 요청이 들어왔는데 해당 사용자의 차례가 아니면 RuntimeException이 발생")
    void completeMission_turnNotCorrect() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member =  new Member(1L, memberUuid);
        Room room = new Room(2L, null, null, null, null, 3L, null, null, null, null);
        Mission mission = new Mission();

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(()->missionService.completeMission(memberUuid, position, roomCode))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 사용자의 차례가 아닙니다.");

    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 게임이 끝나지 않음")
    void completeMission_gameNotOver() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member =  new Member(1L, memberUuid);
        Member opponent = new Member(7L, null);
        Room room = new Room(2L, null, null, null, null, 1L, 2L, null, null, true);
        Mission mission = new Mission();
        Player player = new Player(room, opponent, null);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(room, member)).thenReturn(Collections.EMPTY_LIST);

        //when & then
        MissionCompleteResponse missionCompleteResponse = missionService.completeMission(memberUuid, position, roomCode);

        assertThat(room.getCurrentTurnMemberId()).isEqualTo(7L);
        assertThat(room.getCurrentTurnNumber()).isEqualTo(3L);
        assertThat(room.getTurnStartedAt()).isNotNull();
        assertThat(room.getTurnDeadline()).isNotNull();
        assertThat(room.getCurrentTurnSabotaged()).isFalse();
    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 게임이 끝남")
    void completeMission_gameOver() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member =  new Member(1L, memberUuid);
        Member opponent = new Member(7L, null);
        Room room = new Room(2L, RoomStatus.PLAYING, null, null, null, 1L, 2L, null, null, true);
        Mission mission = new Mission();
        Player player = new Player(room, opponent, null);

        Mission mission0 = new Mission();
        mission0.setPosition(0L);

        Mission mission1 = new Mission();
        mission1.setPosition(1L);

        Mission mission2 = new Mission();
        mission2.setPosition(2L);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(any(), any())).thenReturn(List.of(mission2, mission0, mission1));

        //when & then
        MissionCompleteResponse missionCompleteResponse = missionService.completeMission(memberUuid, position, roomCode);


        assertThat(room.getStatus()).isEqualTo(RoomStatus.FINISHED);
        assertThat(room.getWinner()).isEqualTo(member);
    }
}
