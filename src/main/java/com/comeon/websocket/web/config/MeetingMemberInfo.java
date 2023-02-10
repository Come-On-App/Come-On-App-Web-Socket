package com.comeon.websocket.web.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class MeetingMemberInfo {

    private Long meetingId;
    private Long memberId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String meetingRole;
}
