package com.comeon.websocket.web.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSubscriberResponseData {

    private Long meetingId;
    private List<Long> userIds = new ArrayList<>();
}
