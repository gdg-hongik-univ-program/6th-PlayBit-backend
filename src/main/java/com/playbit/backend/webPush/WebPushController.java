package com.playbit.backend.webPush;

import com.playbit.backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "Subscription API", description = "구독 관련 API입니다.")
@RequestMapping("api/subscriptions")
@RequiredArgsConstructor
public class WebPushController {

    private final WebPushService webPushService;

    @PostMapping
    @Operation(summary = "구독 정보 저장", description = "알림을 허용한 사용자의 구독 정보를 저장합니다.")
    public ResponseEntity<ApiResponse<Subscription>> createSubscription(
            @RequestBody Subscription subscription,
            @RequestHeader(value = "X-Member-Id") String memberUuid)
    {
        URI location = webPushService.createSubscription(subscription, memberUuid);

        return ResponseEntity.created(location).body(ApiResponse.success(subscription));
    }
}
