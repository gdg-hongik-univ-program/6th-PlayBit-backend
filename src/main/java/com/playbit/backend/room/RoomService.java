package com.playbit.backend.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    //PlayerRepository 의존성 주입 필요
    private final RoomRepository roomRepository;

    //방 입장하기
    @Transactional
    public Room enterRoom(String entryCode, String memberUuid){

        // 입장코드 이용한 방 검증
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다.") );

        //uuid 이용한 사용자 검증 로직 (아직 playerRepository는 진행 안됨)
        /*boolean isRegisteredPlayer = playerRepository.
                existsByRoomAndMember_MemberUuid(room, memberUuid);
        if (!isRegisteredPlayer) {
            throw new RuntimeException("이 방에 참여 중인 플레이어가 아닙니다.");
        }*/

        return room;
    }
}
