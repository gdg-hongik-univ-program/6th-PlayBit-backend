package com.playbit.backend.player;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import com.playbit.backend.player.dto.RoomListResponse;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomService;
import com.playbit.backend.room.RoomStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @Transactional
    public PlayerJoinResponse registerPlayer(String entryCode, Member member) {

        Room room = roomRepository
                .findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // 1) 이미 종료된 게임방인지 검증
        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new BadRequestException(ErrorCode.ROOM_FINISHED);
        }

        // 2) 해당 멤버가 이미 이 방의 플레이어인지 확인
        Optional<Player> existingPlayer = playerRepository.findByRoomAndMember(room, member);
        if (existingPlayer.isPresent()) {
            return PlayerJoinResponse.from(existingPlayer.get());
        }

        // 3) 현재 방에 등록된 플레이어 수 확인 및 정원 초과 검증
        long playerCount = playerRepository.countByRoom(room);
        PlayerRole role;

        if (playerCount == 0) {
            role = PlayerRole.O;
        } else if (playerCount == 1) {
            role = PlayerRole.X;
            // 2번째 플레이어 참가 시 게임 자동 시작
            roomService.startGame(room, member);
        } else {
            throw new BadRequestException(ErrorCode.PLAYER_ROOM_IS_ALREADY_FULL);
        }

        Player player = new Player(room, member, role);
        playerRepository.save(player);
        return PlayerJoinResponse.from(player);
    }

    @Transactional
    public void leaveRoom(String entryCode, Member member) {
        Room room = roomRepository
                .findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        Player player = playerRepository
                .findByRoomAndMember(room, member)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));

        playerRepository.delete(player);

        long remaining = playerRepository.countByRoom(room);
        if (remaining == 0) {
            roomRepository.delete(room);
        }
    }

    @Transactional(readOnly = true)
    public RoomListResponse getRooms(Member member) {
        List<RoomListResponse.RoomInfo> rooms = playerRepository.findByMember(member).stream()
                .map(Player::getRoom)
                .map(RoomListResponse.RoomInfo::fromRoom)
                .toList();

        return new RoomListResponse(rooms);
    }
}