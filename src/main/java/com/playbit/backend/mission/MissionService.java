package com.playbit.backend.mission;

import com.playbit.backend.common.event.*;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
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

    // RoomService.setRoom에서 호출하는 카테고리별 9개 미션 자동 생성 메서드
    public void createMission(Category category, Room room) {
        List<Content> missions = getMissionsByCategory(category);
        List<Mission> missionList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            missionList.add(new Mission(room, (long) (i + 1), missions.get(i)));
        }
        missionRepository.saveAll(missionList);
    }

    private List<Content> getMissionsByCategory(Category category) {
        List<Content> missions = Arrays.stream(Content.values())
                .filter(content -> content.getCategory() == category)
                .collect(Collectors.toList());

        Collections.shuffle(missions);
        return missions;
    }

    public boolean isGameOver(Room room, Member member) {
        List<Long> list = missionRepository.findByRoomAndCompletedBy(room, member).stream()
                .map(Mission::getPosition)
                .sorted()
                .toList();

        Set<Long> inputSet = new HashSet<>(list);
        return targetCombinations.stream().anyMatch(inputSet::containsAll);
    }

    @Transactional
    public MissionCompleteResponse completeMission(
            Member member, long position, String roomCode, MultipartFile image, String comment) {
        MissionContext context = validateAndGetContext(member, roomCode, position);
        Room room = context.room();
        Mission mission = context.mission();
        Player opponent = context.opponent();

        List<Member> roomMembers = playerRepository.findByRoom(room).stream()
                .map(Player::getMember)
                .toList();

        if (room.getCurrentTurnMemberId().equals(member.getMemberId())) {
            String imageUrl = s3UploadService.uploadImage(image, "missions");
            applicationEventPublisher.publishEvent(new ImageSaveFailedEvent(imageUrl));
            mission.completeMission(member, imageUrl, comment);

            // 미션 성공 이벤트 발행 (스트릭/성공 수 갱신 리스너로 전달)
            applicationEventPublisher.publishEvent(new MissionSuccessEvent(roomCode, member));

            MissionCompleteResponse response;

            if (isGameOver(room, member)) {
                room.gameFinished(member);
                response = new MissionCompleteResponse(FinishedRoomDto.from(room), MissionDto.from(mission));
                applicationEventPublisher.publishEvent(new GameEndedEvent(roomCode, roomMembers));

            } else {
                room.turnFinished(opponent.getMember().getMemberId());

                if (room.getCurrentTurnNumber() == 10L) {
                    room.gameFinishedAsDraw();
                    response = new MissionCompleteResponse(FinishedRoomDto.from(room), MissionDto.from(mission));
                    applicationEventPublisher.publishEvent(new GameEndedEvent(roomCode, roomMembers));
                } else {
                    response = new MissionCompleteResponse(PlayingRoomDto.from(room), MissionDto.from(mission));
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
            Member member, long position, String roomCode, MultipartFile image, String comment) {

        MissionContext context = validateAndGetContext(member, roomCode, position);
        Room room = context.room();
        Mission mission = context.mission();
        Player opponent = context.opponent();

        if (room.getCurrentTurnMemberId().equals(member.getMemberId())) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_AT_YOUR_TURN);
        }

        if (mission.getCompletedBy() == null) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_UNCOMPLETED_MISSION);
        }

        if (mission.getCompletedBy().equals(member)) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_YOUR_MISSION);
        }

        if (room.getCurrentTurnSabotaged()) {
            throw new BadRequestException(ErrorCode.ROOM_ALREADY_SABOTAGED_AT_THIS_TURN);
        }

        String sabotageImageUrl = s3UploadService.uploadImage(image, "sabotage");
        applicationEventPublisher.publishEvent(new ImageSaveFailedEvent(sabotageImageUrl));
        mission.sabotageMission(sabotageImageUrl, comment);
        room.missionSabotaged();

        applicationEventPublisher.publishEvent(new MissionSabotagedEvent(roomCode, List.of(opponent.getMember())));

        return new MissionSabotageResponse(PlayingRoomDto.from(room), MissionDto.from(mission));
    }

    private record MissionContext(Room room, Mission mission, Player opponent) {
    }

    private MissionContext validateAndGetContext(Member member, String roomCode, long position) {
        Room room = roomRepository
                .findByEntryCode(roomCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        Mission mission = missionRepository
                .findByRoomAndPosition(room, position)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MISSION_NOT_FOUND));

        Player opponent = playerRepository
                .findByRoomAndMemberNot(room, member)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        return new MissionContext(room, mission, opponent);
    }
}