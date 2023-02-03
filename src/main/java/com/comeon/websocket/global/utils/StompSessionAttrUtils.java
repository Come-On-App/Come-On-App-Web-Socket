package com.comeon.websocket.global.utils;

import java.util.Map;

public class StompSessionAttrUtils {

    private static final String USER_ID_KEY = "userId";
    private static final String TOKEN_KEY = "token";
    private static final String MEETING_ID_KEY = "meetingId";

    public static Long getUserId(Map<String, Object> attributes) {
        return Long.parseLong((String) attributes.get(USER_ID_KEY));
    }

    public static void setUserId(Map<String, Object> attributes, Long userId) {
        attributes.put(USER_ID_KEY, userId);
    }

    public static String getToken(Map<String, Object> attributes) {
        return (String) attributes.get(TOKEN_KEY);
    }

    public static void setToken(Map<String, Object> attributes, String token) {
        attributes.put(TOKEN_KEY, token);
    }

    public static Long getMeetingId(Map<String, Object> attributes) {
        return Long.parseLong((String) attributes.get(MEETING_ID_KEY));
    }

    public static void setMeetingId(Map<String, Object> attributes, Long meetingId) {
        attributes.put(MEETING_ID_KEY, meetingId);
    }
}
