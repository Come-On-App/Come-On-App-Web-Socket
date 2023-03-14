package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingPlaceResourceMessage extends AbstractMeetingResourceUpdatedMessage {

    private Long meetingId;

    private MeetingPlaceResourceMessage(MeetingResourceType meetingResourceType, Long meetingId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
    }

    public static MeetingPlaceResourceMessage create(Long meetingId) {
        return new MeetingPlaceResourceMessage(MeetingResourceType.MEETING_PLACES, meetingId);
    }
}
