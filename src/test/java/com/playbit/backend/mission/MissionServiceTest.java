package com.playbit.backend.mission;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.notification.NotificationService;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.sse.SseService; // 💡 SseService import 추가
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import com.playbit.backend.s3.S3UploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MissionServiceTest {

    // 💡 SseService Mock 객체 추가 (NullPointerException 해결)
    @Mock
    private SseService sseService;

    @Mock
    private S3UploadService s3UploadService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MissionService missionService;

    @Test
    @DisplayName("존재하지 않는 유저가 미션 완료를 누르면 NotFoundException 발생")
    void completeMission_userNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.empty());

        //when & then (🌟 코멘트 파라미터 추가)
        assertThatThrownBy(() -> missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(roomRepository, never()).findByEntryCode(anyString());
        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 방으로 미션 완료 요청이 들어오면 NotFoundException 발생")
    void completeMission_roomNotFound() {
        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(new Member()));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("방을 찾을 수 없습니다.");

        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
        verify(playerRepository, never()).findByRoomAndMemberNot(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 미션으로 미션 완료 요청이 들어오면 NotFoundException이 발생")
    void completeMission_missionNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(new Member()));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(new Room()));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("미션을 찾을 수 없습니다.");

        verify(playerRepository, never()).findByRoomAndMemberNot(any(), any());
    }

    @Test
    @DisplayName("미션 완료 요청이 들어왔는데 상대방이 없으면 NotFoundException이 발생")
    void completeMission_playerNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, memberUuid);
        Room room = new Room(1L, null, null, null, null, 1L, null, null, null, null, null);
        Mission mission = new Mission();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("플레이어를 찾을 수 없습니다.");

        assertThat(mission.getCompletedBy()).isNull();
        assertThat(mission.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("미션 완료 요청이 들어왔는데 해당 사용자의 차례가 아니면 BadRequestException이 발생")
    void completeMission_turnNotCorrect() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, memberUuid);
        Room room = new Room(2L, null, null, null, null, 3L, null, null, null, null, null);
        Mission mission = new Mission();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(() -> missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 사용자의 차례가 아닙니다.");
    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 게임이 끝나지 않음")
    void completeMission_gameNotOver() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, memberUuid);
        Member opponent = new Member(7L, null);
        Room room = new Room(2L, null, null, null, null, 1L, 2L, null, null, true, null);
        Mission mission = new Mission();
        Player player = new Player(room, opponent, null);
        MultipartFile image = mock(MultipartFile.class);
        String imageUrl = "https://s3.amazonaws.com/test-image.jpg";

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(room, member)).thenReturn(Collections.EMPTY_LIST);
        when(s3UploadService.uploadImage(any(), anyString())).thenReturn(imageUrl);

        //when & then
        missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트");

        assertThat(room.getCurrentTurnMemberId()).isEqualTo(7L);
        assertThat(room.getCurrentTurnNumber()).isEqualTo(3L);
        assertThat(room.getTurnStartedAt()).isNotNull();
        assertThat(room.getTurnDeadline()).isNotNull();
        assertThat(room.getCurrentTurnSabotaged()).isFalse();
        assertThat(mission.getImageUrl()).isEqualTo(imageUrl);
        assertThat(mission.getComment()).isEqualTo("완료 코멘트"); // 🌟 저장 검증 추가

        verify(sseService, times(1)).broadcastToRoom(eq(roomCode), any());
    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 승패가 결정되어 게임이 끝남")
    void completeMission_gameOver_Not_Draw() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, memberUuid);
        Member opponent = new Member(7L, null);
        Room room = new Room(2L, RoomStatus.PLAYING, null, null, null, 1L, 2L, null, null, true, null);
        Mission mission = new Mission();
        Player player = new Player(room, opponent, null);
        MultipartFile image = mock(MultipartFile.class);
        String imageUrl = "https://s3.amazonaws.com/test-image.jpg";

        Mission mission0 = new Mission();
        mission0.setPosition(1L);

        Mission mission1 = new Mission();
        mission1.setPosition(2L);

        Mission mission2 = new Mission();
        mission2.setPosition(3L);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(any(), any())).thenReturn(List.of(mission2, mission0, mission1));
        when(s3UploadService.uploadImage(any(), anyString())).thenReturn(imageUrl);

        //when & then
        missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.FINISHED);
        assertThat(room.getWinner()).isEqualTo(member);
        assertThat(room.getIsDraw()).isEqualTo(false);
        assertThat(mission.getImageUrl()).isEqualTo(imageUrl);

        verify(sseService, times(1)).broadcastToRoom(eq(roomCode), any());
    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 게임이 무승부로 끝남")
    void completeMission_gameOver_Draw() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, memberUuid);
        Member opponent = new Member(7L, null);
        Room room = new Room(2L, RoomStatus.PLAYING, null, null, null, 1L, 9L, null, null, true, null);
        Mission mission = new Mission();
        Player player = new Player(room, opponent, null);
        MultipartFile image = mock(MultipartFile.class);
        String imageUrl = "https://s3.amazonaws.com/test-image.jpg";

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(any(), any())).thenReturn(List.of());
        when(s3UploadService.uploadImage(any(), anyString())).thenReturn(imageUrl);

        //when & then
        missionService.completeMission(memberUuid, position, roomCode, image, "완료 코멘트");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.FINISHED);
        assertThat(room.getWinner()).isNull();
        assertThat(room.getIsDraw()).isEqualTo(true);
        assertThat(mission.getImageUrl()).isEqualTo(imageUrl);

        verify(sseService, times(1)).broadcastToRoom(eq(roomCode), any());
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 존재하지 않는 유저가 보낸 요청이면 NotFoundException 발생")
    void sabotageMission_memberNotFound() {

        //given
        String memberUuid = UUID.randomUUID().toString();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(memberUuid)).thenReturn(Optional.empty());

        //when & then (🌟 코멘트 파라미터 추가)
        assertThatThrownBy(() -> missionService.sabotageMission(memberUuid, position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(roomRepository, never()).findByEntryCode(any());
        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 존재하지 않는 방에 보낸 요청이면 NotFoundException 발생")
    void sabotageMission_roomNotFound() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, UUID.randomUUID().toString());
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("방을 찾을 수 없습니다.");

        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 존재하지 않는 미션에 보낸 요청이면 NotFoundException 발생")
    void sabotageMission_missionNotFound() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(1L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();
        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 7L, 5L, turnStartedAt, turnStartedAt.plusDays(1L), false, null);
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("미션을 찾을 수 없습니다.");

        assertThat(room.getCurrentTurnSabotaged()).isFalse();
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 자신의 차례에 보낸 요청이면 BadRequestException 발생")
    void sabotageMission_cannotSabotageAtYourTurn() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(7L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 7L, 5L, turnStartedAt, turnStartedAt.plusDays(1L), false, null);
        Mission mission = new Mission();
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member)).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("자신의 차례에는 사보타주가 불가합니다.");

        assertThat(room.getCurrentTurnSabotaged()).isFalse();
        assertThat(room.getTurnDeadline()).isEqualTo(turnStartedAt.plusDays(1L));
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 아무도 완료하지 않은 미션에 보낸 요청이면 BadRequestException 발생")
    void sabotageMission_cannotSabotageToUncompletedMission() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(7L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 9L, 5L, turnStartedAt, turnStartedAt.plusDays(1L), false, null);

        // 🌟 생성자 11개 맞춤 (뒤에 null 2개 추가)
        Mission mission = new Mission(35L, room, 4L, null, null, null, null, false, null, null, null);
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member)).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("완료되지 않은 미션에는 사보타주가 불가합니다.");

        assertThat(room.getCurrentTurnSabotaged()).isFalse();
        assertThat(room.getTurnDeadline()).isEqualTo(turnStartedAt.plusDays(1L));
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 내가 완료한 미션에 보낸 요청이면 BadRequestException 발생")
    void sabotageMission_cannotSabotageToYourMission() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member member = new Member(7L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 9L, 5L, turnStartedAt, turnStartedAt.plusDays(1L), false, null);

        // 🌟 생성자 11개 맞춤
        Mission mission = new Mission(35L, room, 4L, null, member, null, null, false, null, null, null);
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member)).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("자신이 완료한 미션은 사보타주가 불가합니다.");

        assertThat(room.getCurrentTurnSabotaged()).isFalse();
        assertThat(room.getTurnDeadline()).isEqualTo(turnStartedAt.plusDays(1L));
    }

    @Test
    @DisplayName("사보타주 요청이 들어왔는데 이미 이번 턴에 사보타주를 했으면 RuntimeException 발생")
    void sabotageMission_alreadySabotagedAtThisTurn() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member opponent = new Member(34L, UUID.randomUUID().toString());
        Member member = new Member(7L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 34L, 5L, turnStartedAt, turnStartedAt.plusHours(16L), true, null);

        // 🌟 생성자 11개 맞춤
        Mission mission = new Mission(35L, room, 4L, null, opponent, null, null, false, null, null, null);
        MultipartFile image = mock(MultipartFile.class);

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member)).thenReturn(Optional.of(new Player()));

        //when & then
        assertThatThrownBy(() -> missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "사보타주 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이번 턴에 이미 한 번의 사보타주 기회를 사용하였습니다.");
    }

    @Test
    @DisplayName("사보타주 요청 처리 성공")
    void sabotageMission_success() {

        //given
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Member opponentMember = new Member(34L, UUID.randomUUID().toString());
        Member member = new Member(7L, UUID.randomUUID().toString());
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(41L, RoomStatus.PLAYING, roomCode, null, null, 34L, 5L, turnStartedAt, turnStartedAt.plusDays(1L), false, null);

        Mission mission = new Mission(35L, room, 4L, null, opponentMember, turnStartedAt, "https://s3.amazonaws.com/mission.jpg", false, null, null, null);
        MultipartFile image = mock(MultipartFile.class);
        String sabotageImageUrl = "https://s3.amazonaws.com/sabotage.jpg";

        when(memberRepository.findByMemberUuid(member.getMemberUuid())).thenReturn(Optional.of(member));
        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member)).thenReturn(Optional.of(new Player(room, opponentMember, PlayerRole.O)));
        when(s3UploadService.uploadImage(any(), eq("sabotage"))).thenReturn(sabotageImageUrl);

        //when (🌟 사보타주 코멘트 전송)
        MissionSabotageResponse response = missionService.sabotageMission(member.getMemberUuid(), position, roomCode, image, "허위 인증 사보타주!");

        //then
        assertThat(response.room()).isNotNull();
        assertThat(response.mission().sabotagedByOpponent()).isTrue();
        assertThat(response.mission().sabotageImageUrl()).isEqualTo(sabotageImageUrl);
        assertThat(mission.getSabotageComment()).isEqualTo("허위 인증 사보타주!"); // 🌟 코멘트 저장 검증 추가

        assertThat(room.getCurrentTurnSabotaged()).isTrue();
        assertThat(room.getTurnDeadline()).isEqualTo(turnStartedAt.plusHours(18L));

        verify(sseService, times(1)).broadcastToRoom(eq(roomCode), any());
    }
}