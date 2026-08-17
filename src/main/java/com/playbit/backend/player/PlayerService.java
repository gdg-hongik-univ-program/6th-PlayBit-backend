package com.playbit.backend.player;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.mission.MissionRepository;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import com.playbit.backend.player.dto.RoomListResponse;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomService;
import com.playbit.backend.room.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MissionRepository missionRepository; // 미션 리포지토리 주입
    private final RoomService roomService;

    @Transactional
    public PlayerJoinResponse registerPlayer(String entryCode, Member member) {

        Room room = roomRepository
                .findByEntryCodeWithPessimisticLock(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new BadRequestException(ErrorCode.ROOM_FINISHED);
        }

        Optional<Player> existingPlayer = playerRepository.findByRoomAndMember(room, member);
        if (existingPlayer.isPresent()) {
            return PlayerJoinResponse.from(existingPlayer.get());
        }

        long playerCount = playerRepository.countByRoom(room);
        PlayerRole role;

        if (playerCount == 0) {
            role = PlayerRole.O;
        } else if (playerCount == 1) {
            role = PlayerRole.X;
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
            missionRepository.deleteByRoom(room); // 1. 방에 연관된 9개 미션 삭제 (FK 제약조건 해제)
            roomRepository.delete(room);         // 2. 방 삭제
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