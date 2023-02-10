package com.comeon.websocket.web.message.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {

    MEETING_METADATA("모임 기본 정보"),
    MEETING_MEMBERS("모임 회원 리스트"),
    MEETING_PLACES("모임 장소 리스트"),
    MEETING_VOTING("모임일 투표 리스트"),
    MEETING_CONFIRMED_DATE("모임일 정보"),
    ;

    private final String description;
}
