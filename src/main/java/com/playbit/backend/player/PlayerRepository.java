package com.playbit.backend.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Optional<Player> findByRoomAndMemberNot(long roomId, long memberId);
}
