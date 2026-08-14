package com.playbit.backend.auth;

import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String SESSION_KEY = "LOGIN_MEMBER_ID";
    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean isMemberType = Member.class.isAssignableFrom(parameter.getParameterType())
                || Long.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && isMemberType;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new BadRequestException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        Long memberId = (Long) session.getAttribute(SESSION_KEY);
        if (memberId == null) {
            throw new BadRequestException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        if (Long.class.isAssignableFrom(parameter.getParameterType())) {
            return memberId;
        }

        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));
    }
}