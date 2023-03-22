package com.comeon.websocket.web.message.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StompMessage<T> {

    private MessageType messageType;
    private T data;

    public static <T> StompMessage<T> resourceUpdated(T data) {
        return new StompMessage<>(MessageType.RESOURCE_UPDATED_EVENT, data);
    }

    public static <T> StompMessage<T> meetingSubscribeUsers(T data) {
        return new StompMessage<>(MessageType.MEETING_SUBSCRIBE_USER_LIST, data);
    }

    public static <T> StompMessage<T> meetingSubscribeEvent(T data) {
        return new StompMessage<>(MessageType.SUBSCRIBE_MEETING_EVENT, data);
    }

    public static <T> StompMessage<T> meetingUnsubscribeEvent(T data) {
        return new StompMessage<>(MessageType.UNSUBSCRIBE_MEETING_EVENT, data);
    }

    public static <T> StompMessage<T> dropped(T data) {
        return new StompMessage<>(MessageType.DROPPED, data);
    }
}
