package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingPlaceUnlockResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;
    private Long meetingPlaceId;
    private Long userId;

    private MeetingPlaceUnlockResourceMessage(MeetingResourceType meetingResourceType, Long meetingId, Long meetingPlaceId, Long userId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
        this.meetingPlaceId = meetingPlaceId;
        this.userId = userId;
    }

    public static MeetingPlaceUnlockResourceMessage create(Long meetingId, Long meetingPlaceId, Long userId) {
        return new MeetingPlaceUnlockResourceMessage(MeetingResourceType.MEETING_PLACE_UNLOCK, meetingId, meetingPlaceId, userId);
    }
}
