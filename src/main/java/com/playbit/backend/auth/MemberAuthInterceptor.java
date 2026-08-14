package com.playbit.backend.auth;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class MemberAuthInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BadRequestException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        Long memberId = (Long) session.getAttribute(LoginMemberArgumentResolver.SESSION_KEY);
        if (memberId == null) {
            throw new BadRequestException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        if (!memberRepository.existsById(memberId)) {
            throw new BadRequestException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return true;
    }
}