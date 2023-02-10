package com.comeon.websocket.web.config;

public interface MeetingMemberInfoProvider {

    MeetingMemberInfo getMeetingMemberInfoBy(String token, Long meetingId);
}
