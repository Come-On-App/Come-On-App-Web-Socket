package com.comeon.websocket.global.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RootResourceType {

    MEETING("모임 리소스"),
    ;

    private final String description;
}
