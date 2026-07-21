package com.playbit.backend.room;

import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.Content;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.room.dto.EnterRoomResponse;
import com.playbit.backend.room.dto.RoomCreateResponse;
import com.playbit.backend.room.dto.SetRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public EnterRoomResponse enterRoom(String entryCode, String memberUuid){
        // 입장코드 이용한 방 검증
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 잘못된 입장 코드입니다."));

        // 2. DB에서 플레이어 및 미션 목록 가져오기
        List<Player> players = playerRepository.findByRoom(room);
        List<Mission> missions = missionRepository.findByRoom(room);

        Member member = memberRepository.findByMemberUuid(memberUuid)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));


        // 3. 지연 평가(Lazy Evaluation) - 턴 마감 시간 확인 및 턴 넘김 처리
        if (room.getStatus() == RoomStatus.PLAYING
                && room.getTurnDeadline() != null
                && LocalDateTime.now().isAfter(room.getTurnDeadline())) {

            // 현재 턴이 아닌 사람 = 다음 턴을 받을 상대방 찾기
            Long opponentMemberId = players.stream()
                    .map(p -> p.getMember().getMemberId())
                    .filter(id -> !id.equals(room.getCurrentTurnMemberId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("상대방 플레이어를 찾을 수 없습니다."));

            // 턴 업데이트
            room.setCurrentTurnMemberId(opponentMemberId);
            room.setTurnStartedAt(LocalDateTime.now());
            room.setTurnDeadline(LocalDateTime.now().plusHours(24)); // 다음 턴의 제한 시간 (예: 24시간)
            room.setCurrentTurnSabotaged(false); // 사보타주 상태 초기화
        }

        // 4. Mission 엔티티 -> MissionItem DTO 변환
        List<EnterRoomResponse.MissionItem> missionItems = missions.stream()
                .map(mission -> {
                    // completedBy가 null인지 먼저 확인하고, null이 아니면 memberId를 추출
                    Long completedMemberId = (mission.getCompletedBy() != null)
                            ? mission.getCompletedBy().getMemberId()
                            : null;

                    return new EnterRoomResponse.MissionItem(
                            mission.getPosition(),
                            mission.getContent().getDescription(),
                            completedMemberId,
                            mission.getCompletedAt()
                    );
                })
                .toList();

        // 5. Player 엔티티 -> PlayerItem DTO 변환
        List<EnterRoomResponse.PlayerItem> playerItems = players.stream()
                .map(player -> new EnterRoomResponse.PlayerItem(
                        player.getMember().getMemberId(),
                        player.getRole()
                ))
                .toList();

        // 6. 승자 ID 추출 (진행 중일 때는 null)
        Long winnerId = (room.getWinner() != null) ? room.getWinner().getMemberId() : null;

        // 7. 최종 완성된 DTO 반환
        return new EnterRoomResponse(
                room.getEntryCode(),
                room.getStatus(),
                room.getCategory(),
                member.getMemberId(),
                room.getCurrentTurnMemberId(),
                room.getTurnStartedAt(),
                room.getTurnDeadline(),
                room.getCurrentTurnSabotaged(),
                missionItems,
                playerItems,
                winnerId
        );
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
