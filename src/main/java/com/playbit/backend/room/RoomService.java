package com.playbit.backend.room;

import com.playbit.backend.common.event.GameStartedEvent;
import com.playbit.backend.common.event.RoomUpdatedEvent;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.mission.Mission;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.mission.MissionService;
import com.playbit.backend.player.Player;
import com.playbit.backend.player.PlayerRepository;
import com.playbit.backend.player.PlayerRole;
import com.playbit.backend.room.dto.EnterRoomResponse;
import com.playbit.backend.room.dto.RoomCreateResponse;
import com.playbit.backend.room.dto.SetRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MissionRepository missionRepository;
    private final MissionService missionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void startGame(Room room, Member member) {
        Player firstPlayer = playerRepository
                .findByRoomAndRole(room, PlayerRole.O)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        boolean isOFirst = Math.random() < 0.5;
        Long firstTurnMemberId = isOFirst ? firstPlayer.getMember().getMemberId() : member.getMemberId();

        room.startGame(firstTurnMemberId);

        List<Long> playerIds = playerRepository.findByRoom(room).stream()
                .map(player -> player.getMember().getMemberId())
                .toList();

        eventPublisher.publishEvent(new GameStartedEvent(room.getEntryCode(), playerIds));
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void roomCheck() {
        List<Player> players = playerRepository.findAllExpiredPlayingPlayersWithFetchJoin(LocalDateTime.now());

        Map<Room, List<Player>> roomToPlayersMap = players.stream()
                .collect(Collectors.groupingBy(Player::getRoom));

        for (Map.Entry<Room, List<Player>> entry : roomToPlayersMap.entrySet()) {
            checkRoomStatus(entry.getKey(), entry.getValue());
        }
    }

    @Transactional
    public void checkRoomStatus(Room room, List<Player> players) {
        boolean isRoomUpdated = false;

        if (room.getStatus() == RoomStatus.PLAYING
                && room.getTurnDeadline() != null
                && LocalDateTime.now().isAfter(room.getTurnDeadline())) {

            Long opponentMemberId = players.stream()
                    .map(p -> p.getMember().getMemberId())
                    .filter(id -> !id.equals(room.getCurrentTurnMemberId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_OPPONENT_NOT_FOUND));

            room.turnFinished(opponentMemberId);
            isRoomUpdated = true;

            if (isRoomUpdated) {
                eventPublisher.publishEvent(new RoomUpdatedEvent(room.getEntryCode()));
            }
        }
    }

    @Transactional
    public EnterRoomResponse getRoomInfo(String entryCode, Member member) {
        Room room = roomRepository
                .findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));
        List<Player> players = playerRepository.findByRoom(room);
        List<Mission> missions = missionRepository.findByRoom(room);

        List<EnterRoomResponse.MissionItem> missionItems =
                missions.stream().map(EnterRoomResponse.MissionItem::from).toList();

        List<EnterRoomResponse.PlayerItem> playerItems =
                players.stream().map(EnterRoomResponse.PlayerItem::from).toList();

        Long winnerId = (room.getWinner() != null) ? room.getWinner().getMemberId() : null;

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

        do {
            code.delete(0, code.length());
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (roomRepository.findByEntryCode(code.toString()).isPresent());

        return code.toString();
    }

    @Transactional
    public RoomCreateResponse createRoom(Member member) {
        String code = createRoomCode();
        Room room = new Room(RoomStatus.WAITING, null, code);
        roomRepository.save(room);

        List<RoomCreateResponse.CategoryItem> categoryItemList = Arrays.stream(Category.values())
                .map(category -> new RoomCreateResponse.CategoryItem(
                        category.name(),
                        category.getDescription()
                ))
                .toList();

        return new RoomCreateResponse(code, categoryItemList);
    }

    @Transactional
    public SetRoomResponse setRoom(String entryCode, Member member, Category category, String roomName) {
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