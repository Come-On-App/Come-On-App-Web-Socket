package com.comeon.websocket.web.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.*;

@Getter
@NoArgsConstructor
@RedisHash("meeting_subscribe_members")
public class MeetingSubscribeMembers {

    @Id
    private Long meetingId;
    private List<UserSessions> sessionUsers = new ArrayList<>();

    public MeetingSubscribeMembers(Long meetingId) {
        this.meetingId = meetingId;
    }

    public boolean addMember(String sessionId, Long userId) {
        Optional<UserSessions> optUserSessions = this.sessionUsers.stream()
                .filter(userSessions -> userSessions.getUserId().equals(userId))
                .findFirst();

        if (optUserSessions.isPresent()) {
            optUserSessions.get().addSession(sessionId);
            return false;
        }

        UserSessions userSessions = new UserSessions(userId);
        userSessions.addSession(sessionId);
        this.sessionUsers.add(userSessions);
        return true;
    }

    public boolean removeMember(Long userId, String sessionId) {
        UserSessions uSessions = this.sessionUsers.stream()
                .filter(userSessions -> userSessions.getUserId().equals(userId))
                .findFirst()
                .orElseThrow();

        uSessions.getSessionIds().remove(sessionId);

        if (uSessions.getSessionIds().isEmpty()) {
            return this.sessionUsers.remove(uSessions);
        }

        return false;
    }

    @Getter
    @NoArgsConstructor
    public static class UserSessions {
        private Long userId;
        private Set<String> sessionIds = new HashSet<>();

        public UserSessions(Long userId) {
            this.userId = userId;
        }

        public void addSession(String sessionId) {
            this.sessionIds.add(sessionId);
        }
    }
}
