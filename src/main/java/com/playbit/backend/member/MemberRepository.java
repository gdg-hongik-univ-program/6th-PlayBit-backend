package com.playbit.backend.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberUuid(String memberUuid);

    boolean existsByMemberUuid(String memberUuid);

    Optional<Member> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    Optional<Member> findByMemberId(Long memberId);
}
