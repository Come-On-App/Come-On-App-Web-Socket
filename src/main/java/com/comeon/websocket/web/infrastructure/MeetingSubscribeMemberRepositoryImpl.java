package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.config.kafka.producer.KafkaProducer;
import com.comeon.websocket.config.kafka.KafkaTopicProperties;
import com.comeon.websocket.web.config.MeetingSubscribeMemberRepository;
import com.comeon.websocket.web.config.MeetingSubscribeMembers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class MeetingSubscribeMemberRepositoryImpl implements MeetingSubscribeMemberRepository {

    private final KafkaTopicProperties topicProperties;
    private final KafkaProducer producer;
    private final MeetingSubscribeMembersRedisRepository meetingSubscribeMembersRedisRepository;

    public MeetingSubscribeMembers saveMemberAtMeeting(Long meetingId, String sessionId, Long userId) {
        MeetingSubscribeMembers meetingSubscribeMembers = meetingSubscribeMembersRedisRepository.findById(meetingId)
                .orElse(new MeetingSubscribeMembers(meetingId));

        boolean isNewMember = meetingSubscribeMembers.addMember(sessionId, userId);
        meetingSubscribeMembersRedisRepository.save(meetingSubscribeMembers);

        if (isNewMember) {
            producer.produce(topicProperties.getConnectingMembers(), meetingSubscribeMembers);
        }

        return meetingSubscribeMembers;
    }

    public MeetingSubscribeMembers removeMemberAtMeeting(Long meetingId, Long userId, String sessionId) {
        return removeMember(meetingId, userId, sessionId);
    }

    private MeetingSubscribeMembers removeMember(Long meetingId, Long userId, String sessionId) {
        MeetingSubscribeMembers meetingSubscribeMembers = meetingSubscribeMembersRedisRepository.findById(meetingId)
                .orElseThrow();

        boolean memberRemoved = meetingSubscribeMembers.removeMember(userId, sessionId);
        if (meetingSubscribeMembers.getSessionUsers().isEmpty()) {
            meetingSubscribeMembersRedisRepository.delete(meetingSubscribeMembers);
        } else {
            meetingSubscribeMembersRedisRepository.save(meetingSubscribeMembers);
        }

        if (memberRemoved) {
            producer.produce(topicProperties.getConnectingMembers(), meetingSubscribeMembers);
        }

        return meetingSubscribeMembers;
    }

    public void removeMemberAtAllMeetings(List<Long> meetingIds, Long userId, String sessionId) {
        for (Long meetingId : meetingIds) {
            removeMember(meetingId, userId, sessionId);
        }
    }
}
