package com.comeon.websocket.global.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    RESOURCE_UPDATED_EVENT("리소스 업데이트 이벤트 알림"),
    DATA_RESPONSE_MESSAGE("데이터 응답 메시지"),
    ;

    private final String description;
}
