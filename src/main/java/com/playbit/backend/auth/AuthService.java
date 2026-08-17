package com.playbit.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.ErrorCode;
import com.playbit.backend.member.Member;
import com.playbit.backend.member.MemberRepository;
import com.playbit.backend.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Transactional
    public MemberDto loginWithGoogleIdToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new BadRequestException(ErrorCode.ID_TOKEN_REQUIRED);
        }

        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);

        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        
        log.info("구글 토큰 검증 성공 - 이메일: {}", email);

        Optional<Member> existingMember = memberRepository.findByGoogleSub(googleSub);
        Member member;
        
        if (existingMember.isPresent()) {
            member = existingMember.get();
            log.info("기존 유저 로그인 처리 완료 (memberId: {})", member.getMemberId());
        } else {
            member = memberRepository.save(Member.builder().googleSub(googleSub).email(email).build());
            log.info("신규 유저 가입 처리 완료 (memberId: {})", member.getMemberId());
        }

        return MemberDto.from(member);
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier.Builder verifierBuilder =
                    new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance());

            if (googleClientId != null && !googleClientId.isBlank()) {
                verifierBuilder.setAudience(Collections.singletonList(googleClientId));
            }

            GoogleIdTokenVerifier verifier = verifierBuilder.build();
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                log.warn("구글 토큰 검증 실패: 유효하지 않은 토큰입니다.");
                throw new BadRequestException(ErrorCode.INVALID_GOOGLE_ID_TOKEN);
            }

            return idToken.getPayload();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("구글 토큰 검증 중 예외 발생: {}", e.getMessage());
            throw new BadRequestException(ErrorCode.INVALID_GOOGLE_ID_TOKEN);
        }
    }
}