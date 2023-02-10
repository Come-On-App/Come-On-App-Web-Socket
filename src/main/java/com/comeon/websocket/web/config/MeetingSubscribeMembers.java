package com.comeon.websocket.web.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@RedisHash("meeting_subscribe_members")
public class MeetingSubscribeMembers {

    @Id
    private Long meetingId;

    private Set<Long> userIds = new HashSet<>();

    public MeetingSubscribeMembers(Long meetingId) {
        this.meetingId = meetingId;
        this.userIds = new HashSet<>();
    }

    public void addMember(Long userId) {
        this.userIds.add(userId);
    }

    public void removeMember(Long userId) {
        this.userIds.remove(userId);
    }
}
