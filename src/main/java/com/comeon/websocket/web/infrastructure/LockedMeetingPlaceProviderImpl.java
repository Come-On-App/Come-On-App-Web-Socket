package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.message.controller.LockedMeetingPlaceListResponse;
import com.comeon.websocket.web.message.controller.LockedMeetingPlaceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockedMeetingPlaceProviderImpl implements LockedMeetingPlaceProvider {

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public LockedMeetingPlaceListResponse getLockedMeetingPlaceList(Long meetingId) {
        CircuitBreaker cb = circuitBreakerFactory.create("getLockedMeetingPlaces");

        return cb.run(
                () -> comeOnApiUserFeignClient.lockedMeetingPlaceList(meetingId),
                throwable -> {
                    log.error(throwable.getMessage());
                    throw new RuntimeException(throwable);
                }
        );
    }
}
