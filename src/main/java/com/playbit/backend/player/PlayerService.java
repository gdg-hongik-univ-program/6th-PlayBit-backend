package com.playbit.backend.player;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
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
    public Player registerPlayer(String entryCode, String memberUuid){
        //방 검증 로직
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다."));

        //uuid로 멤버 검증 로직
        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        //정원 초과 검증 로직
        long playerCount = playerRepository.countByRoom(room);
        if(playerCount >= 2){
            throw new RuntimeException("정원 초과된 방입니다.");
        }

        //게임 상태 검증 로직
        if(room.getStatus() == RoomStatus.FINISHED) {
            throw new RuntimeException("종료된 게임입니다.");
        }

        // 방 생성자인지 확인

        //player(X 역할) 등록
        Player player = new Player(room, member, PlayerRole.X);
        playerRepository.save(player);

        //방 설정 (선공 player 결정 및 방 정보 업데이트)
        if(playerCount == 1){
            //방 생성자 정보 가져오기
            Player playerO = playerRepository.findByRoomAndRole(room, PlayerRole.O)
                    .orElseThrow(()-> new RuntimeException("방 생성자를 찾을 수 없습니다."));
            //50% 확률로 선공할 멤버 id 결정(동시성 이슈 존재)
            boolean isOFirst = Math.random() < 0.5;
            Long firstTurnMemberId = isOFirst
                    ? playerO.getMember().getMemberId()
                    : member.getMemberId();

            room.startGame(firstTurnMemberId);
        }
        return player;
    }
}
