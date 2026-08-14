package com.playbit.backend.mission;

import com.playbit.backend.member.Member;
import com.playbit.backend.room.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByRoomAndPosition(Room room, Long position);

    List<Mission> findByRoomAndCompletedBy(Room room, Member member);

    List<Mission> findByRoom(Room room);

    // 방 삭제 시 연관된 9개 미션 일괄 삭제용
    void deleteByRoom(Room room);
}