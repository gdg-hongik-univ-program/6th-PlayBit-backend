package com.playbit.backend.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public String createMember() {

        // v4 uuid 생성 (완전히 랜덤 -> 조회 로직 성능 저하 야기할 가능성이 있음)
        UUID uuid = UUID.randomUUID();

        // uuid 중복 확률은 극히 드물어 성능을 위해 중복 검사 로직 생략

        Member member = new Member(uuid.toString());
        memberRepository.save(member);

        return uuid.toString();
    }
}
