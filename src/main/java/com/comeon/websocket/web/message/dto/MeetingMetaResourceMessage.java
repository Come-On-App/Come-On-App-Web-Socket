package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingMetaResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;

    private MeetingMetaResourceMessage(MeetingResourceType meetingResourceType, Long meetingId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
    }

    public static MeetingMetaResourceMessage create(Long meetingId) {
        return new MeetingMetaResourceMessage(MeetingResourceType.MEETING_METADATA, meetingId);
    }
}
