package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    // 해당 사용자 턴이 맞는지 검사 (아직 24시간이 넘어가는 일은 없다고 가정)
    public boolean isYourTurn(Room room, Member member) {

        LocalDateTime now = LocalDateTime.now();

        return now.isBefore(room.getTurnDeadline());
    }

    public boolean isGameOver(Room room, Member member) {

        List<Mission> byRoomIdAndCompletedByMemberId = missionRepository.findByRoomAndCompletedBy(room.getRoomId(), member);

        for(Mission mission : byRoomIdAndCompletedByMemberId) {

        }


        return true;
    }

    @Transactional
    public void completeMission(String memberUuid, long position, String roomCode) {

        // uuid로 멤버를 조회한다 (uuid를 사용해 조회하면 성능 이슈)
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 멤버입니다."));

        // roomCode로 방을 조회한다.
        Room room = roomRepository.findByEntryCode(roomCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

        // roomCode와 position으로 mission을 조회한다.
        Mission mission = missionRepository.findByRoomAndPosition(room.getRoomId(), position)
                .orElseThrow(() -> new RuntimeException(("존재하지 않는 미션입니다.")));

        // 같은 방의 상대방을 조회한다.
        Player opponent = playerRepository.findByRoomAndMemberNot(room.getRoomId(), member.getMemberId())
                .orElseThrow(() -> new RuntimeException(("존재하지 않는 플레이어입니다.")));

        // 해당 사용자의 턴이 맞는지 검사한다.
        if(isYourTurn(room, member)) {

            // 해당 칸을 해당 멤버 아이디로 채운다.
            mission.setCompletedBy(member);

            // 게임이 끝났는지 검사한다.
            if(isGameOver(room, member)) {
                //방 상태를 finished로 바꾸고 승자 기록
                room.setStatus(RoomStatus.FINISHED);
                room.setWinner(member);

            } else {
                // 상대의 턴으로 넘기고 시간을 기록한다
                room.setCurrentTurnMemberId(opponent.getPlayerId());
                room.setTurnStartedAt(LocalDateTime.now());
            }
        } else  {
            throw new RuntimeException("해당 사용자의 차례가 아닙니다.");
        }
    }

    @Transactional
    public void sabotageMission(String memberUuid, long position, String roomCode) {

        // uuid로 멤버를 조회한다 (uuid를 사용해 조회하면 성능 이슈)
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 멤버입니다."));

        // roomCode로 방을 조회한다.
        Room room = roomRepository.findByEntryCode(roomCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

        if(isYourTurn(room, member)) {throw new RuntimeException("자신의 차례에는 사보타주가 불가합니다.");}

        // roomCode와 position으로 mission을 조회한다.
        Mission mission = missionRepository.findByRoomAndPosition(room.getRoomId(), position)
                .orElseThrow(() -> new RuntimeException(("존재하지 않는 미션입니다.")));

        if(mission.getCompletedBy()==null || mission.getCompletedBy()==member) {
            throw new RuntimeException("상대방이 완료한 미션만 사보타주 가능합니다.");
        }

        if(mission.getSabotaged()==true) {
            throw new RuntimeException("이미 사보타주된 미션입니다.");
        }

        mission.setSabotaged(true);

        room.setTurnDeadline(room.getTurnDeadline().minusHours(6));
    }
}
