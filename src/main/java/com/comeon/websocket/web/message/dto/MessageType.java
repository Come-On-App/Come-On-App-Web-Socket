package com.comeon.websocket.web.message.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    RESOURCE_UPDATED_EVENT("리소스 업데이트 이벤트 알림"),
    MEETING_SUBSCRIBE_USER_LIST("모임을 구독중인 유저 리스트"),
    SUBSCRIBE_MEETING_EVENT("모임 구독 이벤트 알림"),
    UNSUBSCRIBE_MEETING_EVENT("모임 구독 해제 이벤트 알림"),
    DROPPED("모임 강퇴 알림"),
    ;

    private final String description;
}
