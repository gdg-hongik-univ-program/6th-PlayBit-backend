package com.playbit.backend.mission;

import com.playbit.backend.common.event.GameEndedEvent;
import com.playbit.backend.common.event.MissionCompletedEvent;
import com.playbit.backend.common.event.MissionSabotagedEvent;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDto;
import com.playbit.backend.mission.dto.MissionSabotageResponse;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Category;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.dto.FinishedRoomDto;
import com.playbit.backend.room.dto.PlayingRoomDto;
import com.playbit.backend.s3.S3UploadService;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final S3UploadService s3UploadService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final List<Set<Long>> targetCombinations = List.of(
            Set.of(1L, 2L, 3L),
            Set.of(4L, 5L, 6L),
            Set.of(7L, 8L, 9L),
            Set.of(1L, 4L, 7L),
            Set.of(2L, 5L, 8L),
            Set.of(3L, 6L, 9L),
            Set.of(1L, 5L, 9L),
            Set.of(3L, 5L, 7L));

    public void createMission(Category category, Room room) {
        // 미션 객체 생성 후 DB에 저장 (batch 저장)
        List<Content> missions = getMissionsByCategory(category);
        List<Mission> missionList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            missionList.add(new Mission(room, (long) (i + 1), missions.get(i)));
        }
        missionRepository.saveAll(missionList);
    }

    // 카테고리에 따라 미션 내용 반환해주는 헬퍼 메서드
    private List<Content> getMissionsByCategory(Category category) {
        List<Content> missions = Arrays.stream(Content.values())
                .filter(content -> content.getCategory() == category)
                .collect(Collectors.toList());

        Collections.shuffle(missions);

        return missions;
    }

    public boolean isGameOver(Room room, Member member) {
        // 해당 멤버가 완료한 칸의 position들을 가져와 배열에 오름차순으로 저장
        List<Long> list = missionRepository.findByRoomAndCompletedBy(room, member).stream()
                .map(Mission::getPosition)
                .sorted()
                .toList();

        // 가져온 리스트를 '집합(Set)'으로 변환 (검색 속도 O(1)로 향상)
        Set<Long> inputSet = new HashSet<>(list);

        return targetCombinations.stream().anyMatch(inputSet::containsAll);
    }

    @Transactional
    public MissionCompleteResponse completeMission(
            String memberUuid, long position, String roomCode, MultipartFile image, String comment) {
        MissionContext context = validateAndGetContext(memberUuid, roomCode, position);
        Member member = context.member();
        Room room = context.room();
        Mission mission = context.mission();
        Player opponent = context.opponent();

        List<Member> roomMembers = playerRepository.findByRoom(room).stream()
                .map(Player::getMember)
                .toList();

        // 해당 사용자의 턴이 맞는지 검사한다.
        if (room.getCurrentTurnMemberId().equals(member.getMemberId())) {

            // S3에 이미지 업로드
            String imageUrl = s3UploadService.uploadImage(image, "missions");
            // 해당 칸을 해당 멤버 아이디와 사진 URL로 채우고 (코멘트는 선택),  시간을 기록한다.
            mission.completeMission(member, imageUrl, comment);
            // 미션 성공 시 이벤트 발행 (스트릭 및 총 성공 수는 이벤트 리스너에서 처리)
            applicationEventPublisher.publishEvent(
                    new com.playbit.backend.common.event.MissionSuccessEvent(roomCode, member));

            MissionCompleteResponse response; // 💡 응답을 미리 담아둘 변수 선언

            // 게임이 끝났는지 검사한다.
            if (isGameOver(room, member)) {
                // 방 상태를 finished로 바꾸고 승자 기록
                room.gameFinished(member);
                response = new MissionCompleteResponse(FinishedRoomDto.from(room), MissionDto.from(mission));

                // 게임 종료 이벤트 발행
                applicationEventPublisher.publishEvent(new GameEndedEvent(roomCode, roomMembers));

            } else {
                room.turnFinished(opponent.getMember().getMemberId());

                // 만약 9개 칸이 다 채워졌는데 무승부이면
                if (room.getCurrentTurnNumber() == 10L) {
                    room.gameFinishedAsDraw();
                    response = new MissionCompleteResponse(FinishedRoomDto.from(room), MissionDto.from(mission));

                    // 게임 종료 이벤트 발행
                    applicationEventPublisher.publishEvent(new GameEndedEvent(roomCode, roomMembers));
                } else {
                    response = new MissionCompleteResponse(PlayingRoomDto.from(room), MissionDto.from(mission));

                    // 미션 완료 이벤트 발행
                    applicationEventPublisher.publishEvent(
                            new MissionCompletedEvent(roomCode, List.of(opponent.getMember())));
                }
            }

            return response;

        } else {
            throw new BadRequestException(ErrorCode.ROOM_NOT_YOUR_TURN);
        }
    }

    @Transactional
    public MissionSabotageResponse sabotageMission(
            String memberUuid, long position, String roomCode, MultipartFile image, String comment) {

        MissionContext context = validateAndGetContext(memberUuid, roomCode, position);
        Member member = context.member();
        Room room = context.room();
        Mission mission = context.mission();
        Player opponent = context.opponent();

        // 자기 턴에 사보타주 요청이 오면 에러 발생
        if (room.getCurrentTurnMemberId().equals(member.getMemberId())) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_AT_YOUR_TURN);
        }

        // 아무도 완료하지 않았거나, 자신이 완료한 미션에 사보타주 요청을 보내면 에러 발생
        if (mission.getCompletedBy() == null) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_UNCOMPLETED_MISSION);
        }

        if (mission.getCompletedBy().equals(member)) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_YOUR_MISSION);
        }

        // 이미 이번 턴에 사보타주를 한 번 했다면 에러 발생
        if (room.getCurrentTurnSabotaged()) {
            throw new BadRequestException(ErrorCode.ROOM_ALREADY_SABOTAGED_AT_THIS_TURN);
        }

        // S3 sabotage/ 경로로 사보타주 사진 업로드
        String sabotageImageUrl = s3UploadService.uploadImage(image, "sabotage");

        // 미션 엔티티에 사보타주 완료 이미지 및 URL 저장 (코멘트는 선택)
        mission.sabotageMission(sabotageImageUrl, comment);

        room.missionSabotaged();

        // 사보타주 완료 이벤트 발행
        applicationEventPublisher.publishEvent(new MissionSabotagedEvent(roomCode, List.of(opponent.getMember())));

        return new MissionSabotageResponse(PlayingRoomDto.from(room), MissionDto.from(mission));
    }

    private record MissionContext(Member member, Room room, Mission mission, Player opponent) {}

    private MissionContext validateAndGetContext(String memberUuid, String roomCode, long position) {
        Member member = memberRepository
                .findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        Room room = roomRepository
                .findByEntryCode(roomCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        Mission mission = missionRepository
                .findByRoomAndPosition(room, position)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MISSION_NOT_FOUND));

        Player opponent = playerRepository
                .findByRoomAndMemberNot(room, member)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        return new MissionContext(member, room, mission, opponent);
    }
}
