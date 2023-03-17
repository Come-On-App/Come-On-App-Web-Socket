package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.config.UserLockRemoveSupporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLockRemoveSupporterImpl implements UserLockRemoveSupporter {

    @Value("${admin.key}")
    private String adminKey;

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public void userLockRemove(Long userId) {
        log.debug("UserLockRemoveSupporter.userLockRemove(userId={})", userId);
        CircuitBreaker cb = circuitBreakerFactory.create("userLockRemove");
        cb.run(
                () -> comeOnApiUserFeignClient.userLockRemove(adminKey, new UserLockRemoveRequest(userId)),
                throwable -> {
                    log.error(throwable.getMessage());
                    throw new RuntimeException(throwable);
                }
        );
    }
}
