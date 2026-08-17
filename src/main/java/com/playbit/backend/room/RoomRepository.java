package com.playbit.backend.room;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByEntryCode(String entryCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.entryCode = :entryCode")
    Optional<Room> findByEntryCodeWithPessimisticLock(@Param("entryCode") String entryCode);

    Optional<Room> findByRoomName(String roomName);

    boolean existsByRoomName(String roomName);

    List<Room> findByStatus(RoomStatus roomStatus);
}
