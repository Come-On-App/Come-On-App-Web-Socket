package com.comeon.websocket.user.application;

public interface MeetingMemberInfoProvider {

    MeetingMemberInfo getMeetingMemberInfoBy(String token, Long meetingId);
}
