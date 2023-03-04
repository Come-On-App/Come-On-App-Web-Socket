package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.config.kafka.producer.KafkaProducer;
import com.comeon.websocket.config.kafka.KafkaTopicProperties;
import com.comeon.websocket.web.config.MeetingSubscribeMemberRepository;
import com.comeon.websocket.web.config.MeetingSubscribeMembers;
import com.comeon.websocket.web.message.dto.MeetingSubUnsubKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
            MeetingSubUnsubKafkaMessage kafkaMessage = MeetingSubUnsubKafkaMessage.createSubMessage(
                    meetingId,
                    userId,
                    meetingSubscribeMembers.getSessionUsers().stream()
                            .map(MeetingSubscribeMembers.UserSessions::getUserId)
                            .collect(Collectors.toSet())
            );
            producer.produce(topicProperties.getConnectingMembers(), kafkaMessage);
        }

        return meetingSubscribeMembers;
    }

    public MeetingSubscribeMembers removeMemberAtMeeting(Long meetingId, Long userId, String sessionId) {
        return removeMember(meetingId, userId, sessionId);
    }

    private MeetingSubscribeMembers removeMember(Long meetingId, Long userId, String sessionId) {
        log.debug("removeMember at meetingId: {}, userId: {}, sessionId: {}", meetingId, userId, sessionId);
        MeetingSubscribeMembers meetingSubscribeMembers = meetingSubscribeMembersRedisRepository.findById(meetingId)
                .orElseThrow();

        boolean memberRemoved = meetingSubscribeMembers.removeMember(userId, sessionId);
        if (meetingSubscribeMembers.getSessionUsers().isEmpty()) {
            meetingSubscribeMembersRedisRepository.delete(meetingSubscribeMembers);
        } else {
            meetingSubscribeMembersRedisRepository.save(meetingSubscribeMembers);
        }

        if (memberRemoved) {
            MeetingSubUnsubKafkaMessage kafkaMessage = MeetingSubUnsubKafkaMessage.createUnsubMessage(
                    meetingId,
                    userId,
                    meetingSubscribeMembers.getSessionUsers().stream()
                            .map(MeetingSubscribeMembers.UserSessions::getUserId)
                            .collect(Collectors.toSet())
            );
            producer.produce(topicProperties.getConnectingMembers(), kafkaMessage);
        }

        return meetingSubscribeMembers;
    }

    public void removeMemberAtAllMeetings(List<Long> meetingIds, Long userId, String sessionId) {
        for (Long meetingId : meetingIds) {
            removeMember(meetingId, userId, sessionId);
        }
    }
}
