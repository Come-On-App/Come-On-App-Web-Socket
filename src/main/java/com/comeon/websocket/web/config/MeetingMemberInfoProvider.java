package com.comeon.websocket.web.config;

public interface MeetingMemberInfoProvider {

    MeetingMemberInfo getMeetingMemberInfoBy(Long meetingId, Long userId);
}
