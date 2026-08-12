package com.playbit.backend.room;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByEntryCode(String entryCode);

    Optional<Room> findByRoomName(String roomName);
}
