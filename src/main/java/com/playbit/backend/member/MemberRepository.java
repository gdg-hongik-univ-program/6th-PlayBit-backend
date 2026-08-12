package com.playbit.backend.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberUuid(String memberUuid);

    boolean existsByNickname(String nickname);

    boolean existsByMemberUuid(String memberUuid);
}
