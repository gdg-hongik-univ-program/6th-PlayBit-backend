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

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        // 사전 요청(OPTIONS)은 무조건 통과 (CORS 에러 방지)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 헤더 X-Member-Id에서 값을 꺼냄
        String memberUuid = request.getHeader("X-Member-Id");

        // 헤더 값이 비어있다면 401 에러
        if (memberUuid == null || memberUuid.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "X-Member-Id 헤더가 필요합니다.");
            return false;
        }

        // DB에 이 UUID를 가진 멤버가 있는지 확인
        boolean isExistUser = memberRepository.existsByMemberUuid(memberUuid);

        if (!isExistUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않는 유저입니다.");
            return false;
        }

        return true;
    }
}