package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingFixedDateResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;

    private MeetingFixedDateResourceMessage(MeetingResourceType meetingResourceType, Long meetingId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
    }

    public static MeetingFixedDateResourceMessage create(Long meetingId) {
        return new MeetingFixedDateResourceMessage(MeetingResourceType.MEETING_FIXED_DATE, meetingId);
    }
}
