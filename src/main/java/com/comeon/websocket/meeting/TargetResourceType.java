package com.comeon.websocket.meeting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetResourceType {
    META_DATA("모임 기본 정보"),
    MEMBERS("모임 회원 리스트"),
    PLACES("모임 장소 리스트"),
    VOTING("모임일 투표 리스트"),
    FIXED_DATE("모임일 정보"),
    ;

    private final String description;
}
