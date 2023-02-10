package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.config.UserInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoProviderImpl implements UserInfoProvider {

    private static final String PREFIX_BEARER = "Bearer ";

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public Long getUserIdBy(String token) {
        CircuitBreaker cb = circuitBreakerFactory.create("getUserIdCircuitBreaker");
        UserDetailsResponse userDetailsResponse = cb.run(
                () -> comeOnApiUserFeignClient.getUserDetails(PREFIX_BEARER + token),
                throwable -> { // TODO 예외 처리
                    log.error(throwable.getMessage());
                    return null;
                }
        );

        if (userDetailsResponse == null) {
            return null;
        }

        return userDetailsResponse.getUserId();
    }
}
