package com.playbit.backend.room;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByEntryCode(String entryCode);

    Optional<Room> findByRoomName(String roomName);

    boolean existsByRoomName(String roomName);

    List<Room> findByStatus(RoomStatus roomStatus);
}
