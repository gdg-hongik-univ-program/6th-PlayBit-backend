package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByRoomAndPosition(Room room, Long position);
    List<Mission> findByRoomAndCompletedBy(Room room, Member member);
}
