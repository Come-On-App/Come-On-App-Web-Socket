package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.message.controller.LockedMeetingPlaceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "come-on-api-user-feign-client",
        url = "${comeon.api-server.url}"
)
public interface ComeOnApiUserFeignClient {

    @GetMapping(value = "/api/v1/users/me")
    UserDetailsResponse getUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);

    @GetMapping(value = "/api/v1/meetings/{meetingId}/members/me")
    MemberInfoResponse getMemberInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                     @PathVariable Long meetingId);

    @GetMapping(value = "/api/v2/meetings/{meetingId}/places/lock")
    LockedMeetingPlaceListResponse lockedMeetingPlaceList(@PathVariable Long meetingId);
}
