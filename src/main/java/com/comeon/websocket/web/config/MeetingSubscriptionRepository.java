package com.comeon.websocket.web.config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@NoArgsConstructor
public class MeetingSubscriptionRepository {

    // {meetingId, {users}}
    private ConcurrentHashMap<Long, List<User>> repository = new ConcurrentHashMap<>();

    public boolean addSession(Long meetingId, Long userId, String sessionId, String subscriptionId) {
        List<User> meetingUsers = this.repository.get(meetingId);

        // meetingId와 일치하는 모임이 없으면 모임 저장소 생성
        if (meetingUsers == null) {
            List<User> value = new ArrayList<>();
            value.add(new User(userId, sessionId, subscriptionId));
            this.repository.put(meetingId, value);
            return true;
        }

        // 모임에 회원 리스트가 비어있으면 유저 추가
        if (meetingUsers.isEmpty()) {
            User user = new User(userId, sessionId, subscriptionId);
            meetingUsers.add(user);
            return true;
        }

        Optional<User> userOpt = meetingUsers.stream()
                .filter(user -> user.userId.equals(userId))
                .findFirst();

        // userId와 일치하는 유저가 없으면 생성
        if (userOpt.isEmpty()) {
            meetingUsers.add(new User(userId, sessionId, subscriptionId));
            return true;
        }

        // userId와 일치하는 유저가 있으면 추가
        User user = userOpt.get();
        user.addSubscription(sessionId, subscriptionId);
        return false;
    }

    public boolean removeSession(Long meetingId, Long userId, String sessionId, String subscriptionId) {
        log.debug("meetingId: {}, userId: {}, sessionId: {}, subscriptionId: {}", meetingId, userId, sessionId, subscriptionId);
        List<User> meetingUsers = this.repository.get(meetingId);
        Optional<User> userOpt = meetingUsers.stream().filter(user -> user.userId.equals(userId))
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.removeSubscription(sessionId, subscriptionId);
            if (user.sessions.isEmpty()) {
                meetingUsers.remove(user);
                log.debug("return true");
                return true;
            }
        }

        log.debug("return false");
        return false;
    }

    public List<Long> getUsersAtMeeting(Long meetingId) {
        return repository.get(meetingId).stream().map(user -> user.userId).collect(Collectors.toList());
    }

    private static class User {
        private Long userId;
        private List<Session> sessions = new ArrayList<>();

        public User(Long userId, String sessionId, String subscriptionId) {
            this.userId = userId;
            this.sessions.add(new Session(sessionId, subscriptionId));
        }

        public void addSubscription(String sessionId, String subscriptionId) {
            Optional<Session> sessionOpt = this.sessions.stream()
                    .filter(session -> session.sessionId.equals(sessionId))
                    .findFirst();

            if (sessionOpt.isEmpty()) {
                // sessionId와 일치하는 세션이 없으면 생성
                this.sessions.add(new Session(sessionId, subscriptionId));
            } else {
                // sessionId와 일치하는 세션이 있으면 subscriptionId 추가
                sessionOpt.get().addSubscriptionId(subscriptionId);
            }
        }

        public void removeSubscription(String sessionId, String subscriptionId) {
            Optional<Session> sessionOpt = this.sessions.stream().filter(session -> session.sessionId.equals(sessionId))
                    .findFirst();

            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                session.removeSubscriptionId(subscriptionId);
                if (session.subscriptionIds.isEmpty()) {
                    sessions.remove(session);
                }
            }
        }
    }

    private static class Session {
        private String sessionId;
        private List<String> subscriptionIds = new ArrayList<>();

        public Session(String sessionId, String subscriptionId) {
            this.sessionId = sessionId;
            this.subscriptionIds.add(subscriptionId);
        }

        public void addSubscriptionId(String subscriptionId) {
            this.subscriptionIds.add(subscriptionId);
        }

        public void removeSubscriptionId(String subscriptionId) {
            this.subscriptionIds.remove(subscriptionId);
        }
    }
}
