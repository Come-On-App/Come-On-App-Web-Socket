package com.comeon.websocket.web.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class LockedMeetingPlaceListMessage {

    private Long meetingId;
    private List<LockedPlaceSimple> lockedPlaces;

    private LockedMeetingPlaceListMessage(Long meetingId, List<LockedPlaceSimple> lockedPlaces) {
        this.meetingId = meetingId;
        this.lockedPlaces = lockedPlaces;
    }

    public static LockedMeetingPlaceListMessage create(Long meetingId, List<LockedPlaceSimple> lockedPlaces) {
        return new LockedMeetingPlaceListMessage(meetingId, lockedPlaces);
    }

    @Getter
    @AllArgsConstructor
    public static class LockedPlaceSimple {

        private Long meetingPlaceId;
        private Long lockingUserId;
    }
}
