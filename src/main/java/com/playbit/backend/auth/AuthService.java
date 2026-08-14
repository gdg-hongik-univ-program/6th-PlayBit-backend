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

import java.util.Collections;

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

        Member member = memberRepository
                .findByGoogleSub(googleSub)
                .orElseGet(() -> memberRepository.save(
                        Member.builder().googleSub(googleSub).email(email).build()));

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
                throw new BadRequestException(ErrorCode.INVALID_GOOGLE_ID_TOKEN);
            }

            return idToken.getPayload();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_GOOGLE_ID_TOKEN);
        }
    }
}