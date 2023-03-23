package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.message.controller.LockedMeetingPlaceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "come-on-api-user-feign-client",
        url = "${comeon.api-server.url}"
)
public interface ComeOnApiUserFeignClient {

    @GetMapping(value = "/api/v1/admin/users")
    UserDetailsResponse getUserDetails(@RequestHeader("admin-key") String adminKey,
                                       @RequestParam Long userId);

    @GetMapping(value = "/api/v1/admin/meetings/{meetingId}/members")
    MemberInfoResponse getMemberInfo(@RequestHeader("admin-key") String adminKey,
                                     @PathVariable Long meetingId,
                                     @RequestParam Long userId);

    @GetMapping(value = "/api/v1/admin/meetings/{meetingId}/places/lock")
    LockedMeetingPlaceListResponse lockedMeetingPlaceList(@RequestHeader("admin-key") String adminKey, @PathVariable Long meetingId);

    @PostMapping(value = "/api/v1/admin/meeting-place/unlock")
    UserLockRemoveResponse userLockRemove(@RequestHeader("admin-key") String adminKey, @RequestBody UserLockRemoveRequest request);
}
