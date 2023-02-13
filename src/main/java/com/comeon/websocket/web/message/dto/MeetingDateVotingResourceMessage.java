package com.comeon.websocket.web.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MeetingDateVotingResourceMessage extends AbstractMeetingResourceUpdatedMessage{

    private Long meetingId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate targetDate;

    private MeetingDateVotingResourceMessage(MeetingResourceType meetingResourceType, Long meetingId, LocalDate targetDate) {
        super(meetingResourceType);
        this.meetingId = meetingId;
        this.targetDate = targetDate;
    }

    public static MeetingDateVotingResourceMessage create(Long meetingId, LocalDate date) {
        return new MeetingDateVotingResourceMessage(MeetingResourceType.MEETING_VOTING, meetingId, date);
    }
}
