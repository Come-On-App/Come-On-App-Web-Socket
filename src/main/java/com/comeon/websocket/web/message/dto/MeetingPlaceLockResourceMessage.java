package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingPlaceLockResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;
    private Long meetingPlaceId;
    private Long userId;

    private MeetingPlaceLockResourceMessage(MeetingResourceType meetingResourceType, Long meetingId, Long meetingPlaceId, Long userId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
        this.meetingPlaceId = meetingPlaceId;
        this.userId = userId;
    }

    public static MeetingPlaceLockResourceMessage create(Long meetingId, Long meetingPlaceId, Long userId) {
        return new MeetingPlaceLockResourceMessage(MeetingResourceType.MEETING_PLACE_LOCK, meetingId, meetingPlaceId, userId);
    }
}
