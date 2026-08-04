package com.playbit.backend.webPush;

import com.playbit.backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.martijndwars.webpush.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@Tag(name = "Subscription API", description = "알림 관련 API입니다.")
@RequestMapping("api/subscriptions")
public class WebPushController {

    private WebPushService webPushService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSubscription(
            Subscription subscription,
            @RequestHeader(value = "X-Member-Id") String memberUuid)
    {
        URI location = webPushService.createSubscription(subscription, memberUuid);

        return ResponseEntity.created(location).body(ApiResponse.success(subscription));
    }
}
