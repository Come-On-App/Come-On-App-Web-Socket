package com.comeon.websocket.global.message;

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

    public static <T> StompMessage<T> dataResponse(T data) {
        return new StompMessage<>(MessageType.DATA_RESPONSE_MESSAGE, data);
    }
}
