package com.comeon.websocket.web.message.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberDroppedMessage {

    private Long meetingId;
    private Long userId;

    public static MemberDroppedMessage create(Long meeting, Long userId) {
        return new MemberDroppedMessage(meeting, userId);
    }
}
