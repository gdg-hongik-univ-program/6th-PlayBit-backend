package com.playbit.backend.mission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import com.playbit.backend.s3.S3UploadService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class MissionServiceTest {

    @Mock
    private S3UploadService s3UploadService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private MissionService missionService;

    @Test
    @DisplayName("존재하지 않는 방으로 미션 완료 요청이 들어오면 NotFoundException 발생")
    void completeMission_roomNotFound() {
        Member member = Member.builder().memberId(1L).googleSub("12345").build();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MultipartFile image = mock(MultipartFile.class);

        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> missionService.completeMission(member, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("방을 찾을 수 없습니다.");

        verify(missionRepository, never()).findByRoomAndPosition(any(), anyLong());
    }

    @Test
    @DisplayName("미션 완료 요청이 들어왔는데 해당 사용자의 차례가 아니면 BadRequestException이 발생")
    void completeMission_turnNotCorrect() {
        Member member = Member.builder().memberId(1L).googleSub("12345").build();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(2L, null, null, null, null, "테스트방", 3L, null, null, null, null, null);
        Mission mission = new Mission(10L, room, 1L, Content.STUDY_1, null, null, null, false, null, null, null);
        MultipartFile image = mock(MultipartFile.class);

        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(new Player()));

        assertThatThrownBy(() -> missionService.completeMission(member, position, roomCode, image, "완료 코멘트"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 사용자의 차례가 아닙니다.");
    }

    @Test
    @DisplayName("올바른 미션 완료 요청이 들어오고 게임이 끝나지 않음")
    void completeMission_gameNotOver() {
        Member member = Member.builder().memberId(1L).googleSub("12345").build();
        Member opponent = Member.builder().memberId(7L).googleSub("67890").build();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(2L, null, null, null, null, "테스트방", 1L, 2L, null, null, true, null);
        Mission mission = new Mission(10L, room, 1L, Content.STUDY_1, null, null, null, false, null, null, null);
        Player player = new Player(room, opponent, null);
        MultipartFile image = mock(MultipartFile.class);
        String imageUrl = "https://s3.amazonaws.com/test-image.jpg";

        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(any(), any())).thenReturn(Optional.of(player));
        when(missionRepository.findByRoomAndCompletedBy(room, member)).thenReturn(Collections.emptyList());
        when(s3UploadService.uploadImage(any(), anyString())).thenReturn(imageUrl);

        missionService.completeMission(member, position, roomCode, image, "완료 코멘트");

        assertThat(room.getCurrentTurnMemberId()).isEqualTo(7L);
        assertThat(room.getCurrentTurnNumber()).isEqualTo(3L);
        assertThat(mission.getImageUrl()).isEqualTo(imageUrl);
        assertThat(mission.getComment()).isEqualTo("완료 코멘트");
    }

    @Test
    @DisplayName("사보타주 요청 처리 성공")
    void sabotageMission_success() {
        Member member = Member.builder().memberId(7L).googleSub("12345").build();
        Member opponentMember = Member.builder().memberId(34L).googleSub("67890").build();
        long position = 0L;
        String roomCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime turnStartedAt = LocalDateTime.now();

        Room room = new Room(
                41L,
                RoomStatus.PLAYING,
                roomCode,
                null,
                null,
                "테스트방",
                34L,
                5L,
                turnStartedAt,
                turnStartedAt.plusDays(1L),
                false,
                null);

        Mission mission = new Mission(
                35L,
                room,
                4L,
                Content.STUDY_1,
                opponentMember,
                turnStartedAt,
                "https://s3.amazonaws.com/mission.jpg",
                false,
                null,
                null,
                null);
        MultipartFile image = mock(MultipartFile.class);
        String sabotageImageUrl = "https://s3.amazonaws.com/sabotage.jpg";

        when(roomRepository.findByEntryCode(roomCode)).thenReturn(Optional.of(room));
        when(missionRepository.findByRoomAndPosition(any(), anyLong())).thenReturn(Optional.of(mission));
        when(playerRepository.findByRoomAndMemberNot(room, member))
                .thenReturn(Optional.of(new Player(room, opponentMember, PlayerRole.O)));
        when(s3UploadService.uploadImage(any(), eq("sabotage"))).thenReturn(sabotageImageUrl);

        MissionSabotageResponse response =
                missionService.sabotageMission(member, position, roomCode, image, "허위 인증 사보타주!");

        assertThat(response.room()).isNotNull();
        assertThat(response.mission().sabotagedByOpponent()).isTrue();
        assertThat(response.mission().sabotageImageUrl()).isEqualTo(sabotageImageUrl);
        assertThat(mission.getSabotageComment()).isEqualTo("허위 인증 사보타주!");
        assertThat(room.getCurrentTurnSabotaged()).isTrue();
    }
}