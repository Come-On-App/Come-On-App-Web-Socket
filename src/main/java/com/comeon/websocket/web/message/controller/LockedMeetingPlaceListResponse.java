package com.comeon.websocket.web.message.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LockedMeetingPlaceListResponse {

    private List<LockedPlaceSimple> lockedPlaces;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockedPlaceSimple {

        private Long meetingPlaceId;
        private Long lockingUserId;
    }
}
