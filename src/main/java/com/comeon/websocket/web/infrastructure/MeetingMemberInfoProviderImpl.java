package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.config.MeetingMemberInfo;
import com.comeon.websocket.web.config.MeetingMemberInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingMemberInfoProviderImpl implements MeetingMemberInfoProvider {

    @Value("${admin.key}")
    private String adminKey;

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final ComeOnApiUserFeignClient comeOnApiUserFeignClient;

    @Override
    public MeetingMemberInfo getMeetingMemberInfoBy(Long meetingId, Long userId) {
        CircuitBreaker cb = circuitBreakerFactory.create("getMemberInfo");
        MemberInfoResponse memberInfoResponse = cb.run(
                () -> comeOnApiUserFeignClient.getMemberInfo(adminKey, meetingId, userId),
                throwable -> { // TODO 예외 처리
                    log.error(throwable.getMessage());
                    return null;
                }
        );

        if (memberInfoResponse == null) {
            return null;
        }

        return new MeetingMemberInfo(
                meetingId,
                memberInfoResponse.getMemberId(),
                memberInfoResponse.getUserId(),
                memberInfoResponse.getNickname(),
                memberInfoResponse.getProfileImageUrl(),
                memberInfoResponse.getMemberRole()
        );
    }
}
