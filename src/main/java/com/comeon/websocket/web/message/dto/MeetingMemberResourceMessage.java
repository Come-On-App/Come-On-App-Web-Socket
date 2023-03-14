package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingMemberResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;

    private MeetingMemberResourceMessage(MeetingResourceType meetingResourceType, Long meetingId) {
        super(meetingResourceType);
        this.meetingId = meetingId;
    }

    public static MeetingMemberResourceMessage create(Long meetingId) {
        return new MeetingMemberResourceMessage(MeetingResourceType.MEETING_MEMBERS, meetingId);
    }
}
