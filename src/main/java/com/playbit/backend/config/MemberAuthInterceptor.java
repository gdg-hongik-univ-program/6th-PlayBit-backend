package com.playbit.backend.config;

import com.playbit.backend.member.MemberRepository; // 본인 경로에 맞게 확인
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class MemberAuthInterceptor implements HandlerInterceptor{

    // playbit의 MemberRepository를 주입받아 DB를 확인해
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        // 사전 요청(OPTIONS)은 무조건 통과 (CORS 에러 방지)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 1. playbit에서 약속한 헤더 이름 'X-Member-Id'에서 값을 꺼냄
        String memberUuid = request.getHeader("X-Member-Id");

        // 2. 헤더 값이 비어있다면 401 에러
        if (memberUuid == null || memberUuid.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "X-Member-Id 헤더가 필요합니다.");
            return false;
        }

        // 3. DB에 이 UUID(memberUuid)를 가진 유저가 있는지 확인
        // (MemberRepository에 existsByMemberUuid 메서드가 있다고 가정)
        boolean isExistUser = memberRepository.existsByMemberUuid(memberUuid);

        if (!isExistUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않는 유저입니다.");
            return false;
        }

        return true;
    }
}