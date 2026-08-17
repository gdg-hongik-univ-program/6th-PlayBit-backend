package com.playbit.backend.member;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleSub(String googleSub);

    boolean existsByNickname(String nickname);

    List<Member> findAllByMemberIdIn(List<Long> memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Optional<Member> findByIdWithPessimisticLock(@Param("memberId") Long memberId);
}