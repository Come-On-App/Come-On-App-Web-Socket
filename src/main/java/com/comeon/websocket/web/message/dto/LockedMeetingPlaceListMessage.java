package com.comeon.websocket.web.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class LockedMeetingPlaceListMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;
    private List<LockedPlaceSimple> lockedPlaces;

    private LockedMeetingPlaceListMessage(MeetingResourceType meetingResourceType, Long meetingId, List<LockedPlaceSimple> lockedPlaces) {
        super(meetingResourceType);
        this.meetingId = meetingId;
        this.lockedPlaces = lockedPlaces;
    }

    public static LockedMeetingPlaceListMessage create(Long meetingId, List<LockedPlaceSimple> lockedPlaces) {
        return new LockedMeetingPlaceListMessage(MeetingResourceType.LOCKED_MEETING_PLACE_LIST, meetingId, lockedPlaces);
    }

    @Getter
    @AllArgsConstructor
    public static class LockedPlaceSimple {

        private Long meetingPlaceId;
        private Long lockingUserId;
    }
}
