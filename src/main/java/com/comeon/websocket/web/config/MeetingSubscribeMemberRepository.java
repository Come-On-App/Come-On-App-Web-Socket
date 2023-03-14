package com.comeon.websocket.web.config;

import java.util.List;

public interface MeetingSubscribeMemberRepository {

    MeetingSubscribeMembers saveMemberAtMeeting(Long meetingId, String sessionId, Long userId);
    MeetingSubscribeMembers removeMemberAtMeeting(Long meetingId, Long userId, String sessionId);
    void removeMemberAtAllMeetings(List<Long> meetingIds, Long userId, String sessionId);
}
