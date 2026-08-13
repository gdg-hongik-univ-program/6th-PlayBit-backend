package com.playbit.backend.player;

import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public PlayerJoinResponse registerPlayer(String entryCode, Member member) {
        Room room = roomRepository.findByEntryCode(entryCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        Optional<Player> existingPlayer = playerRepository.findByRoomAndMember(room, member);
        if (existingPlayer.isPresent()) {
            return new PlayerJoinResponse(
                    existingPlayer.get().getPlayerId(),
                    existingPlayer.get().getMember().getMemberId(),
                    existingPlayer.get().getRole().name()
            );
        }

        long playerCount = playerRepository.countByRoom(room);
        PlayerRole role = (playerCount == 0) ? PlayerRole.O : PlayerRole.X;

        Player player = new Player(room, member, role);
        playerRepository.save(player);

        return new PlayerJoinResponse(
                player.getPlayerId(),
                player.getMember().getMemberId(),
                player.getRole().name()
        );
    }
}