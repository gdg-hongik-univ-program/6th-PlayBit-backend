package com.playbit.backend.player;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PlayerJoinResponse registerPlayer(String entryCode, String memberUuid){
        //방 검증 로직
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다."));

        //uuid로 멤버 검증 로직
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        //게임 상태 검증 로직
        if(room.getStatus() == RoomStatus.FINISHED) {
            throw new RuntimeException("종료된 게임입니다.");
        }
        //중복 참가 방지 로직 (혼자서 O, X 다 하는 것 방지)
        if (playerRepository.existsByRoomAndMember(room, member)) {
            throw new RuntimeException("이미 방에 참가한 사용자입니다.");
        }

        //현재 방에 등록된 플레이어 수 확인
        long playerCount = playerRepository.countByRoom(room);
        PlayerRole role;

        //역할 부여 및 정원 초가 검증
        if (playerCount == 0) {
            // 첫 번째 접속자(O 역할)
            role = PlayerRole.O;
        } else if (playerCount == 1) {
            // 두 번째 접속자(X 역할)
            role = PlayerRole.X;
        } else {
            // 2명 이상일 경우
            throw new RuntimeException("정원 초과된 방입니다.");
        }

        //player DB에 저장
        Player player = new Player(room, member, role);
        playerRepository.save(player);

        //선공 player 결정 및 게임 시작
        if(playerCount == 1){
            //방 생성자 정보 가져오기 -> 방 생성자가 무조건 O를 가져와야 할까?
            Player firstPlayer = playerRepository.findByRoomAndRole(room, PlayerRole.O)
                    .orElseThrow(()-> new RuntimeException("기존 플레이어를 찾을 수 없습니다."));
            //50% 확률로 선공할 멤버 id 결정(동시성 이슈 존재)
            boolean isOFirst = Math.random() < 0.5;
            Long firstTurnMemberId = isOFirst
                    ? firstPlayer.getMember().getMemberId()
                    : member.getMemberId();

            room.startGame(firstTurnMemberId);
        }
        return new PlayerJoinResponse(
                player.getPlayerId(),
                player.getMember().getMemberId(),
                player.getRole().name()
        );
    }
}
