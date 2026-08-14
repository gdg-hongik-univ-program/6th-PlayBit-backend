package com.playbit.backend.auth;

import com.playbit.backend.auth.dto.GoogleLoginRequest;
import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.common.exception.NotFoundException;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.member.dto.MemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 및 구글 로그인 관련 API입니다.")
public class AuthController {

    private final AuthService authService;
    private final MemberRepository memberRepository;

    @PostMapping("/google")
    @Operation(summary = "구글 로그인", description = "구글 ID Token을 검증하여 로그인 처리하고 세션(JSESSIONID)을 발급합니다.")
    public ResponseEntity<ApiResponse<MemberDto>> loginWithGoogle(
            @RequestBody GoogleLoginRequest request, HttpServletRequest httpServletRequest) {
        MemberDto memberDTO = authService.loginWithGoogleIdToken(request.idToken());

        // HTTP 세션 생성 및 사용자 PK 저장 (JSESSIONID 쿠키 발급)
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(LoginMemberArgumentResolver.SESSION_KEY, memberDTO.memberId());

        return ResponseEntity.ok(ApiResponse.success(memberDTO));
    }

    @GetMapping("/me")
    @Operation(summary = "로그인 유저 정보 조회", description = "현재 세션에 로그인된 사용자의 정보를 반환합니다.")
    public ResponseEntity<ApiResponse<MemberDto>> getMyInfo(@LoginMember Member member) {

        // DB에서 최신 회원 프로필(닉네임) 정보를 조회하여 반환
        Member managedMember = memberRepository.findById(member.getMemberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(MemberDto.from(member)));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 HTTP 세션을 만료시킵니다.")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}