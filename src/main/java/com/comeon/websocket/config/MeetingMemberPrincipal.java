package com.comeon.websocket.config;

import java.security.Principal;

public class MeetingMemberPrincipal implements Principal {

    private final Long userId;

    public MeetingMemberPrincipal(Long userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
