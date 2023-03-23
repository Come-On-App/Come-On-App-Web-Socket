package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingTimeResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;

    private MeetingTimeResourceMessage(MeetingResourceType meetingResourceType, Long meetingId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
    }

    public static MeetingTimeResourceMessage create(Long meetingId) {
        return new MeetingTimeResourceMessage(MeetingResourceType.MEETING_TIME, meetingId);
    }
}
