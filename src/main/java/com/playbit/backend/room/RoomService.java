package com.playbit.backend.room;

import com.playbit.backend.common.event.GameStartedEvent;
import com.playbit.backend.common.event.RoomUpdatedEvent;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.mission.MissionService;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.dto.EnterRoomResponse;
import com.playbit.backend.room.dto.RoomCreateResponse;
import com.playbit.backend.room.dto.SetRoomResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MissionRepository missionRepository;
    private final MissionService missionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void startGame(Room room, Member member) {
        // 방 생성자 정보 가져오기 -> 방 생성자가 무조건 O를 가져와야 할까? -> 그냥 역할 O인 사람 가쟈오기
        Player firstPlayer = playerRepository
                .findByRoomAndRole(room, PlayerRole.O)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));
        // 50% 확률로 선공할 멤버 id 결정(동시성 이슈 존재)
        boolean isOFirst = Math.random() < 0.5;
        Long firstTurnMemberId = isOFirst ? firstPlayer.getMember().getMemberId() : member.getMemberId();

        room.startGame(firstTurnMemberId);

        List<Member> players = playerRepository.findByRoom(room).stream()
                .map(Player::getMember)
                .toList();

        eventPublisher.publishEvent(new GameStartedEvent(room.getEntryCode(), players));
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void roomCheck() {
        List<Room> existingRooms = roomRepository.findByStatus(RoomStatus.PLAYING);
        for (Room room : existingRooms) {
            List<Player> players = playerRepository.findByRoom(room);
            checkRoomStatus(room, players);
        }
    }

    @Transactional
    public void checkRoomStatus(Room room, List<Player> players) {

        // SSE 알림을 위해 방 상태 추적하는 변수
        boolean isRoomUpdated = false;

        // 3. 지연 평가(Lazy Evaluation) - 턴 마감 시간 확인 및 턴 넘김 처리
        if (room.getStatus() == RoomStatus.PLAYING
                && room.getTurnDeadline() != null
                && LocalDateTime.now().isAfter(room.getTurnDeadline())) {

            // 현재 턴이 아닌 사람 = 다음 턴을 받을 상대방 찾기
            Long opponentMemberId = players.stream()
                    .map(p -> p.getMember().getMemberId())
                    .filter(id -> !id.equals(room.getCurrentTurnMemberId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_OPPONENT_NOT_FOUND));

            // 턴 업데이트
            room.turnFinished(opponentMemberId);

            // 턴이 넘어갔으므로 업데이트 변수 true
            isRoomUpdated = true;

            // 업데이트 변수가 true 라면 방 전체 유저에게 화면을 갱신하라고 SSE 알림 발송
            if (isRoomUpdated) {
                eventPublisher.publishEvent(new RoomUpdatedEvent(room.getEntryCode()));
            }
        }
    }

    @Transactional
    public EnterRoomResponse getRoomInfo(String entryCode, String memberUuid) {
        Room room = roomRepository
                .findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));
        List<Player> players = playerRepository.findByRoom(room);
        List<Mission> missions = missionRepository.findByRoom(room);
        Member member = memberRepository
                .findByMemberUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 4. Mission 엔티티 -> MissionItem DTO 변환
        List<EnterRoomResponse.MissionItem> missionItems =
                missions.stream().map(EnterRoomResponse.MissionItem::from).toList();

        // 5. Player 엔티티 -> PlayerItem DTO 변환
        List<EnterRoomResponse.PlayerItem> playerItems =
                players.stream().map(EnterRoomResponse.PlayerItem::from).toList();

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
                winnerId);
    }

    public String createRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        // 중복되지 않는 코드가 생성될 때까지 반복
        do {
            code.delete(0, code.length());
            // 6자리 입장 코드 추출
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (roomRepository.findByEntryCode(code.toString()).isPresent());

        return code.toString();
    }

    @Transactional
    public RoomCreateResponse createRoom(String memberUuid) {

        if (!memberRepository.existsByMemberUuid(memberUuid)) {
            throw new NotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String code = createRoomCode();

        Room room = new Room(RoomStatus.WAITING, null, code);

        roomRepository.save(room);

        // Category Enum의 모든 값을 순회하며 한글 이름까지 추출
        List<RoomCreateResponse.CategoryItem> categoryItemList = Arrays.stream(Category.values())
                .map(category -> new RoomCreateResponse.CategoryItem(
                        category.name(), // "STUDY"
                        category.getDescription() // "공부"
                        ))
                .toList();

        return new RoomCreateResponse(code, categoryItemList);
    }

    // 방 이름, 카테고리 반영 후 방 생성
    @Transactional
    public SetRoomResponse setRoom(String entryCode, String memberUuid, Category category, String roomName) {

        if (!memberRepository.existsByMemberUuid(memberUuid)) {
            throw new NotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Room room = roomRepository
                .findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        if (roomRepository.existsByRoomName(roomName)) {
            throw new BadRequestException(ErrorCode.ROOM_NAME_DUPLICATED);
        }

        room.updateCategory(category);
        room.updateRoomName(roomName);

        missionService.createMission(category, room);

        return new SetRoomResponse();
    }
}
