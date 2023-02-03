package com.comeon.websocket.global.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUpdatedMessage {

    private RootResourceType rootResource;
    private String rootResourceId;
    private String targetResource;
    private String targetResourceId;

    public static ResourceUpdatedMessage meetingResourceOf(Long rootResourceId, Enum targetResource, Long targetResourceId) {
        return new ResourceUpdatedMessage(RootResourceType.MEETING, String.valueOf(rootResourceId), targetResource.name(), String.valueOf(targetResourceId));
    }

    public static ResourceUpdatedMessage meetingResourceOf(Long rootResourceId, Enum targetResource) {
        return new ResourceUpdatedMessage(RootResourceType.MEETING, String.valueOf(rootResourceId), targetResource.name(), null);
    }
}
