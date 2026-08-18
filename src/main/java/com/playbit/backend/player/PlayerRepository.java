package com.playbit.backend.player;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByRoom(Room room);

    Optional<Player> findByRoomAndRole(Room room, PlayerRole role);

    Optional<Player> findByRoomAndMember(Room room, Member member);

    Optional<Player> findByRoomAndMemberNot(Room room, Member member);

    List<Player> findByRoom(Room room);

    List<Player> findByMember(Member member);

    @Query("SELECT p FROM Player p " +
            "JOIN FETCH p.room r " +
            "JOIN FETCH p.member m " +
            "WHERE r.status = 'PLAYING' AND r.turnDeadline < :now")
    List<Player> findAllExpiredPlayingPlayersWithFetchJoin(@Param("now") LocalDateTime now);
}
