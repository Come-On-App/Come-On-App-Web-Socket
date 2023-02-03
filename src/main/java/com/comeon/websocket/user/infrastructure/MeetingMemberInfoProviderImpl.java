package com.comeon.websocket.user.infrastructure;

import com.comeon.websocket.user.application.MeetingMemberInfo;
import com.comeon.websocket.user.application.MeetingMemberInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingMemberInfoProviderImpl implements MeetingMemberInfoProvider {

    private static final String PREFIX_BEARER = "Bearer ";

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public MeetingMemberInfo getMeetingMemberInfoBy(String token, Long meetingId) {
        CircuitBreaker cb = circuitBreakerFactory.create("getMemberInfo");
        MemberInfoResponse memberInfoResponse = cb.run(
                () -> comeOnApiUserFeignClient.getMemberInfo(PREFIX_BEARER + token, meetingId),
                throwable -> { // TODO 예외 처리
                    log.error(throwable.getMessage());
                    return null;
                }
        );

        if (memberInfoResponse == null) {
            return null;
        }

        return new MeetingMemberInfo(
                memberInfoResponse.getMeetingId(),
                memberInfoResponse.getUserId(),
                memberInfoResponse.getNickname(),
                memberInfoResponse.getProfileImageUrl(),
                memberInfoResponse.getMeetingRole()
        );
    }
}
