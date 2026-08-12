package com.playbit.backend.config;

import com.playbit.backend.auth.MemberAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final MemberAuthInterceptor memberAuthInterceptor;

    // ✅ 기존 설정 대신 Filter 단에서 동작하는 CorsFilter를 Bean으로 등록합니다.
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        // 🚨 주의: URL 양옆의 < > 기호는 반드시 제거해야 합니다.
        config.addAllowedOrigin("https://playbit-play-bit.vercel.app");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("https://essential-family-display.ngrok-free.dev");

        // 모든 메서드(GET, POST, PUT, DELETE, OPTIONS 등) 허용
        config.addAllowedMethod("*");

        // 프론트에서 보내는 모든 헤더 허용
        config.addAllowedHeader("*");

        // 프론트엔드에서 응답 헤더를 읽을 수 있도록 노출
        config.addExposedHeader("X-Member-Id");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // ✅ 인터셉터 설정은 그대로 유지합니다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/members");
    }
}
