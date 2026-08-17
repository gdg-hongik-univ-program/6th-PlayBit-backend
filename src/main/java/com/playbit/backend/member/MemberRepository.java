package com.playbit.backend.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleSub(String googleSub);

    boolean existsByNickname(String nickname);

    List<Member> findAllByMemberIdIn(List<Long> memberId);
}