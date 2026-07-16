package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.Content;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.dto.RoomCreateResponse;
import com.playbit.backend.room.dto.SetRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MissionRepository missionRepository;

    //방 입장하기
    @Transactional
    public Room enterRoom(String entryCode, String memberUuid){

        // 입장코드 이용한 방 검증
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다."));

        //uuid 이용한 player 검증 로직
        boolean isRegisteredPlayer = playerRepository.
                existsByRoomAndMember_MemberUuid(room, memberUuid);
        if (!isRegisteredPlayer) {
            throw new RuntimeException("이 방에 참여 중인 플레이어가 아닙니다.");
        }

        return room;
    }

    //방 생성
    @Transactional
    public RoomCreateResponse createRoom(){
        // 입장 코드 랜덤 생성
        String entryCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(RoomStatus.WAITING, null, entryCode);
        roomRepository.save(room);

        //Category Enum의 모든 값을 순회하며 한글 이름까지 추출
        List<RoomCreateResponse.CategoryItem> categoryItemList = Arrays.stream(Category.values())
                .map(category -> new RoomCreateResponse.CategoryItem(
                        category.name(),            // "STUDY"
                        category.getDescription()   // "공부"
                ))
                .toList();

        return new RoomCreateResponse(entryCode,categoryItemList);

    }

    //카테고리 선택후 방 생성
    @Transactional
    public SetRoomResponse setRoom(String entryCode, String memberUuid, Category category){
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다."));

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        //카테고리 업데이트
        room.updateCategory(category);

        //미션 객체 생성 후 DB에 저장
        List<Content> missions = getMissionsByCategory(category);
        for (int i =0; i <9; i++){
            Mission mission = new Mission(room,(long) i, missions.get(i));
            missionRepository.save(mission);
        }

        return new SetRoomResponse();
    }

    // 카테고리에 따라 미션 내용 반환해주는 헬퍼 메서드
    private List<Content> getMissionsByCategory(Category category) {
        List<Content> missions = new ArrayList<>();

        if (category == Category.STUDY) {
            missions.addAll(Arrays.asList(
                    Content.STUDY_1, Content.STUDY_2, Content.STUDY_3,
                    Content.STUDY_4, Content.STUDY_5, Content.STUDY_6,
                    Content.STUDY_7, Content.STUDY_8, Content.STUDY_9));
        }
        else {
            // 일치하는 카테고리가 없을 경우의 기본값
            for (int i = 0; i < 9; i++) {
                missions.add(Content.DEFAULT_MISSION);
            }
        }
        //미션 자동으로 섞기
        Collections.shuffle(missions);

        return missions;
    }
}
