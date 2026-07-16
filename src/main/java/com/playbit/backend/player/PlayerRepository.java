package com.playbit.backend.player;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByRoom(Room room);
    Optional<Player> findByRoomAndRole(Room room, PlayerRole role);
    Optional<Player> findByRoomAndMemberNot(long roomId, long memberId);
    boolean existsByRoomAndMember_MemberUuid(Room room, String memberUuid);
    boolean existsByRoomAndMember(Room room, Member member);
    List<Player> findByRoom(Room room);
}
