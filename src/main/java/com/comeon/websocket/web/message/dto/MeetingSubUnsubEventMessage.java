package com.comeon.websocket.web.message.dto;

import lombok.Getter;

@Getter
public class MeetingSubUnsubEventMessage {

    private Long meetingId;
    private Long targetUserId;

    private MeetingSubUnsubEventMessage(Long meetingId, Long targetUserId) {
        this.meetingId = meetingId;
        this.targetUserId = targetUserId;
    }

    public static MeetingSubUnsubEventMessage create(Long meetingId, Long userId) {
        return new MeetingSubUnsubEventMessage(meetingId, userId);
    }
}
