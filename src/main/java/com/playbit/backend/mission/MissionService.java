package com.playbit.backend.mission;

import com.playbit.backend.common.ErrorCode;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDTO;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.notification.NotificationService;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.dto.FinishedRoomDTO;
import com.playbit.backend.room.dto.PlayingRoomDTO;
import com.playbit.backend.s3.S3UploadService;
import com.playbit.backend.sse.SseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final SseService sseService;
    private final S3UploadService s3UploadService;
    private final NotificationService notificationService;


    public boolean isGameOver(Room room, Member member) {
        // 해당 멤버가 완료한 칸의 position들을 가져와 배열에 오름차순으로 저장
        List<Long> list = missionRepository.findByRoomAndCompletedBy(room, member)
                .stream()
                .map(Mission::getPosition)
                .sorted()
                .toList();

        // 승리하는 경우 등록
        List<Set<Long>> targetCombinations = List.of(
                Set.of(1L, 2L, 3L),
                Set.of(4L, 5L, 6L),
                Set.of(7L, 8L, 9L),
                Set.of(1L, 4L, 7L),
                Set.of(2L, 5L, 8L),
                Set.of(3L, 6L, 9L),
                Set.of(1L, 5L, 9L),
                Set.of(3L, 5L, 7L)
        );

        // 가져온 리스트를 '집합(Set)'으로 변환 (검색 속도 O(1)로 향상)
        Set<Long> inputSet = new HashSet<>(list);

        return targetCombinations.stream()
                .anyMatch(target ->inputSet.containsAll(target));
    }

    @Transactional
    public MissionCompleteResponse completeMission(String memberUuid, long position, String roomCode
            , MultipartFile image, String comment) {
        // uuid로 멤버를 조회한다
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // roomCode로 방을 조회한다.
        Room room = roomRepository.findByEntryCode(roomCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // roomCode와 position으로 mission을 조회한다.
        Mission mission = missionRepository.findByRoomAndPosition(room, position)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MISSION_NOT_FOUND));

        // 같은 방의 상대방을 조회한다.
        Player opponent = playerRepository.findByRoomAndMemberNot(room, member)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        List<Member> roomMembers = playerRepository.findByRoom(room).stream()
                .map(player -> player.getMember()).toList();

        // 해당 사용자의 턴이 맞는지 검사한다.
        if(room.getCurrentTurnMemberId().equals(member.getMemberId())) {

            // S3에 이미지 업로드
            String imageUrl = s3UploadService.uploadImage(image, "missions");
            // 해당 칸을 해당 멤버 아이디와 사진 URL로 채우고 (코멘트는 선택),  시간을 기록한다.
            mission.completeMission(member, imageUrl, comment);

            MissionCompleteResponse response; // 💡 응답을 미리 담아둘 변수 선언

            // 게임이 끝났는지 검사한다.
            if(isGameOver(room, member)) {
                //방 상태를 finished로 바꾸고 승자 기록
                room.gameFinished_Not_Draw(member);
                response = new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission));

                // 게임 종료 알림 보내기
                notificationService.roomFinishedNotification(roomCode, roomMembers);

            } else {
                room.turnFinished(opponent.getMember().getMemberId());

                // 만약 9개 칸이 다 채워졌는데 무승부이면
                if(room.getCurrentTurnNumber() == 10L) {
                    room.gameFinished_Draw();
                    response = new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission));

                    // 게임 종료 알림 보내기
                    notificationService.roomFinishedNotification(roomCode, roomMembers);
                } else {
                    response = new MissionCompleteResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission));

                    // 게임 안 끝나고 턴만 넘어갈 때 알림 발송
                    notificationService.missionCompleteNotification(roomCode, List.of(opponent.getMember()));
                }
            }

            // 💡 리턴하기 직전, 방에 있는 사람들에게 미션 완료 알림 발송
            sseService.broadcastToRoom(roomCode, Map.of("message", "MISSION_COMPLETED"));

            return response;

        } else  {
            throw new BadRequestException(ErrorCode.ROOM_NOT_YOUR_TURN);
        }
    }

    @Transactional
    public MissionSabotageResponse sabotageMission(String memberUuid, long position, String roomCode,
                                                   MultipartFile image, String comment) {

        // uuid로 멤버를 조회한다
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // roomCode로 방을 조회한다.
        Room room = roomRepository.findByEntryCode(roomCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // roomCode와 position으로 mission을 조회한다.
        Mission mission = missionRepository.findByRoomAndPosition(room, position)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MISSION_NOT_FOUND));

        // 같은 방의 상대방을 조회한다.
        Player opponent = playerRepository.findByRoomAndMemberNot(room, member)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        // 자기 턴에 사보타주 요청이 오면 에러 발생
        if(room.getCurrentTurnMemberId().equals(member.getMemberId())) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_AT_YOUR_TURN);
        }

        // 아무도 완료하지 않았거나, 자신이 완료한 미션에 사보타주 요청을 보내면 에러 발생
        if(mission.getCompletedBy() == null) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_UNCOMPLETED_MISSION);
        }

        if(mission.getCompletedBy() == member) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_YOUR_MISSION);
        }

        // 이미 이번 턴에 사보타주를 한 번 했다면 에러 발생
        if(room.getCurrentTurnSabotaged()) {
            throw new BadRequestException(ErrorCode.ROOM_ALREADY_SABOTAGED_AT_THIS_TURN);
        }

        // S3 sabotage/ 경로로 사보타주 사진 업로드
        String sabotageImageUrl = s3UploadService.uploadImage(image, "sabotage");

        // 미션 엔티티에 사보타주 완료 이미지 및 URL 저장 (코멘트는 선택)
        mission.sabotageMission(sabotageImageUrl, comment);

        room.setCurrentTurnSabotaged(true);
        room.setTurnDeadline(room.getTurnDeadline().minusHours(6));

        // 💡 리턴하기 직전, 사보타주 발생 알림 발송
        sseService.broadcastToRoom(roomCode, Map.of("message", "MISSION_SABOTAGED"));

        // 리턴 전 상대방에게 알림 전송
        notificationService.sabotageCompleteNotification(roomCode, List.of(opponent.getMember()));

        return new MissionSabotageResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission));
    }
}
