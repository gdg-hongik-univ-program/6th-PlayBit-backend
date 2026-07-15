package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByRoomAndPosition(Long roomId, Long position);
    List<Mission> findByRoomAndCompletedBy(Long roomId, Member member);
}
