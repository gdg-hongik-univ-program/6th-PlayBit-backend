package com.playbit.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer{

    private final MemberAuthInterceptor memberAuthInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://playbit.vercel.app", // 프론트 실제 배포 주소
                        "http://localhost:3000",      // 로컬 테스트용
                        "http://localhost:5173",      // 로컬 테스트용(Vite)
                        "https://essential-family-display.ngrok-free.dev" //ngrok
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // 🚨 여기서 "X-Member-Id"를 반드시 열어줘야 프론트에서 보낸 헤더가 백엔드에 도착해!
                .allowedHeaders("X-Member-Id", "Content-Type", "Authorization", "ngrok-skip-browser-warning")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(memberAuthInterceptor)
                .addPathPatterns("/api/**")
                // 🚨 주의: 처음 앱에 들어와서 UUID를 새로 발급받는(가입하는) API 주소는 여기서 꼭 빼줘야 해!
                // 아래는 예시니까, 실제 회원을 생성하는 API 경로로 수정해줘.
                .excludePathPatterns("/api/members");
    }
}