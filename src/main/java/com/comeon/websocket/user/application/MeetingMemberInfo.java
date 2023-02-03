package com.comeon.websocket.user.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class MeetingMemberInfo {

    private Long meetingId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String meetingRole;
}
