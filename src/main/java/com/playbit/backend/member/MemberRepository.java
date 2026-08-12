package com.playbit.backend.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberUuid(String memberUuid);
    boolean existsByMemberUuid(String memberUuid);
    Optional<Member> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    Optional<Member> findByMemberId(Long memberId);
}
