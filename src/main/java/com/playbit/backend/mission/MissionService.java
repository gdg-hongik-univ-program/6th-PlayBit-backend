package com.playbit.backend.mission;

import com.playbit.backend.common.response.ErrorCode;
import com.playbit.backend.common.response.exception.BadRequestException;
import com.playbit.backend.common.response.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.dto.MissionCompleteResponse;
import com.playbit.backend.mission.dto.MissionDTO;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.dto.FinishedRoomDTO;
import com.playbit.backend.room.dto.PlayingRoomDTO;
import com.playbit.backend.room.dto.RoomDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public MissionService (MissionRepository missionRepository, MemberRepository memberRepository, RoomRepository roomRepository, PlayerRepository playerRepository) {
        this.missionRepository = missionRepository;
        this.memberRepository = memberRepository;
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
    }

    public boolean isGameOver(Room room, Member member) {

        // 해당 멤버가 완료한 칸의 position들을 가져와 배열에 오름차순으로 저장
        List<Long> list = missionRepository.findByRoomAndCompletedBy(room, member)
                .stream()
                .map(Mission::getPosition)
                .sorted()
                .toList();

        // 승리하는 경우 등록
        List<Set<Long>> targetCombinations = List.of(
                Set.of(0L, 1L, 2L),
                Set.of(3L, 4L, 5L),
                Set.of(6L, 7L, 8L),
                Set.of(0L, 3L, 6L),
                Set.of(1L, 4L, 7L),
                Set.of(2L, 5L, 8L),
                Set.of(0L, 4L, 8L),
                Set.of(2L, 4L, 6L)
        );

        // 가져온 리스트를 '집합(Set)'으로 변환 (검색 속도 O(1)로 향상)
        Set<Long> inputSet = new HashSet<>(list);

        return targetCombinations.stream()
                .anyMatch(target ->inputSet.containsAll(target));
    }

    @Transactional
    public MissionCompleteResponse completeMission(String memberUuid, long position, String roomCode) {

        // uuid로 멤버를 조회한다 (uuid를 사용해 조회하면 성능 이슈)
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

        // 해당 사용자의 턴이 맞는지 검사한다.
        if(room.getCurrentTurnMemberId().equals(member.getMemberId())) {

            // 해당 칸을 해당 멤버 아이디로 채우고, 시간을 기록한다.
            mission.setCompletedBy(member);
            mission.setCompletedAt(LocalDateTime.now());

            // 게임이 끝났는지 검사한다.
            if(isGameOver(room, member)) {

                //방 상태를 finished로 바꾸고 승자 기록
                room.gameFinished_Not_Draw(member);
                return new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission));

            } else {

                room.turnFinished(opponent.getMember().getMemberId());

                // 만약 9개 칸이 다 채워졌는데 무승부이면
                if(room.getCurrentTurnNumber() == 10L) {
                    room.gameFinished_Draw();
                    return new MissionCompleteResponse(FinishedRoomDTO.from(room), MissionDTO.from(mission));
                }

                return new MissionCompleteResponse(PlayingRoomDTO.from(room), MissionDTO.from(mission));
            }
        } else  {
            throw new BadRequestException(ErrorCode.ROOM_NOT_YOUR_TURN);
        }
    }

    @Transactional
    public RoomDTO sabotageMission(String memberUuid, long position, String roomCode) {

        // uuid로 멤버를 조회한다 (uuid를 사용해 조회하면 성능 이슈)
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // roomCode로 방을 조회한다.
        Room room = roomRepository.findByEntryCode(roomCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // roomCode와 position으로 mission을 조회한다.
        Mission mission = missionRepository.findByRoomAndPosition(room, position)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MISSION_NOT_FOUND));

        if(room.getCurrentTurnMemberId().equals(member.getMemberId()))
        {throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_AT_YOUR_TURN);}

        if(mission.getCompletedBy()==null || mission.getCompletedBy()==member) {
            throw new BadRequestException(ErrorCode.MISSION_CANNOT_SABOTAGE_TO_YOUR_MISSION);
        }

        if(room.getCurrentTurnSabotaged()) {
            throw new BadRequestException(ErrorCode.ROOM_ALREADY_SABOTAGED_AT_THIS_TURN);
        }

        room.setCurrentTurnSabotaged(true);

        room.setTurnDeadline(room.getTurnDeadline().minusHours(6));

        return PlayingRoomDTO.from(room);
    }
}
