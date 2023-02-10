package com.comeon.websocket.web.config;

import java.util.List;

public interface MeetingSubscribeMemberRepository {

    MeetingSubscribeMembers saveMemberAtMeeting(Long meetingId, Long userId);
    MeetingSubscribeMembers removeMemberAtMeeting(Long meetingId, Long userId);
    void removeMemberAtAllMeetings(List<Long> meetingIds, Long userId);
}
