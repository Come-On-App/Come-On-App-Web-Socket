package com.comeon.websocket.web.message.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUpdatedMessage {

    private Long meetingId;
    private ResourceType updatedResourceType;

    public static ResourceUpdatedMessage ofMeetingPlaces(Long meetingId) {
        return new ResourceUpdatedMessage(meetingId, ResourceType.MEETING_PLACES);
    }
}
