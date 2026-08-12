package com.playbit.backend.player;

import com.playbit.backend.common.event.GameStartedEvent;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.player.dto.PlayerJoinResponse;
import com.playbit.backend.player.dto.RoomListResponse;
import com.playbit.backend.room.Room;
import com.playbit.backend.room.RoomRepository;
import com.playbit.backend.room.RoomStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public PlayerJoinResponse registerPlayer(String entryCode, String memberUuid) {
        // 방 검증 로직
        Room room =
                roomRepository
                        .findByEntryCode(entryCode)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // uuid로 멤버 검증 로직
        Member member =
                memberRepository
                        .findByMemberUuid(memberUuid)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 해당 멤버가 이미 이 방의 플레이어인지 확인
        Optional<Player> existingPlayer = playerRepository.findByRoomAndMember(room, member);
        if (existingPlayer.isPresent()) {
            return new PlayerJoinResponse(
                    existingPlayer.get().getPlayerId(),
                    existingPlayer.get().getMember().getMemberId(),
                    existingPlayer.get().getRole().name());
        }

        // 현재 방에 등록된 플레이어 수 확인
        long playerCount = playerRepository.countByRoom(room);
        PlayerRole role;

        // 역할 부여 및 정원 초가 검증
        if (playerCount == 0) {
            // 첫 번째 접속자(O 역할)
            role = PlayerRole.O;
        } else if (playerCount == 1) {
            // 두 번째 접속자(X 역할)
            role = PlayerRole.X;
        } else {
            // 2명 이상일 경우
            throw new BadRequestException(ErrorCode.PLAYER_ROOM_IS_ALREADY_FULL);
        }

        // 게임 상태 검증 로직
        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new BadRequestException(ErrorCode.ROOM_FINISHED);
        }

        // player DB에 저장
        Player player = new Player(room, member, role);
        playerRepository.save(player);

        // 선공 player 결정 및 게임 시작
        if (playerCount == 1) {
            // 방 생성자 정보 가져오기 -> 방 생성자가 무조건 O를 가져와야 할까? -> 그냥 역할 O인 사람 가쟈오기
            Player firstPlayer =
                    playerRepository
                            .findByRoomAndRole(room, PlayerRole.O)
                            .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));
            // 50% 확률로 선공할 멤버 id 결정(동시성 이슈 존재)
            boolean isOFirst = Math.random() < 0.5;
            Long firstTurnMemberId =
                    isOFirst ? firstPlayer.getMember().getMemberId() : member.getMemberId();

            room.startGame(firstTurnMemberId);

            // 해당 방의 모든 멤버 찾기
            List<Member> players =
                    playerRepository.findByRoom(room).stream().map(Player::getMember).toList();

            // 게임 시작 이벤트 발행
            applicationEventPublisher.publishEvent(new GameStartedEvent(entryCode, players));
        }
        return new PlayerJoinResponse(
                player.getPlayerId(), player.getMember().getMemberId(), player.getRole().name());
    }

    /** 현재 사용자가 지정된 방에서 탈퇴합니다. 탈퇴 후 방에 플레이어가 남아있지 않으면 방 자체를 삭제합니다. */
    @Transactional
    public void leaveRoom(String entryCode, String memberUuid) {
        // 1) 방 검증
        Room room =
                roomRepository
                        .findByEntryCode(entryCode)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND));

        // 2) 회원 검증
        Member member =
                memberRepository
                        .findByMemberUuid(memberUuid)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 3) 플레이어(참가자) 조회 & 삭제
        Player player =
                playerRepository
                        .findByRoomAndMember(room, member)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.PLAYER_NOT_FOUND));
        playerRepository.delete(player);

        // 4) 방에 남은 플레이어가 없으면 방 삭제
        long remaining = playerRepository.countByRoom(room);
        if (remaining == 0) {
            roomRepository.delete(room);
        }
    }

    @Transactional
    public RoomListResponse getRooms(String memberUuid) {

        Member member =
                memberRepository
                        .findByMemberUuid(memberUuid)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        List<RoomListResponse.RoomInfo> rooms =
                playerRepository.findByMember(member).stream()
                        .map(player -> player.getRoom())
                        .map(room -> RoomListResponse.RoomInfo.fromRoom(room))
                        .toList();

        return new RoomListResponse(rooms);
    }
}
