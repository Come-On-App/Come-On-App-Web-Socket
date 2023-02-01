package com.comeon.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPrincipalProviderImpl implements UserPrincipalProvider {

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public Principal createUserPrincipalByToken(String token) {
        CircuitBreaker cb = circuitBreakerFactory.create("getUserDetails");
        UserDetailsResponse userDetailsResponse = cb.run(
                () -> comeOnApiUserFeignClient.getUserDetails("Bearer " + token),
                throwable -> { // TODO 예외 처리
                    log.error(throwable.getMessage());
                    return null;
                }
        );

        if (userDetailsResponse == null) {
            return null;
        }
        return new MeetingMemberPrincipal(userDetailsResponse.getUserId());
    }
}
