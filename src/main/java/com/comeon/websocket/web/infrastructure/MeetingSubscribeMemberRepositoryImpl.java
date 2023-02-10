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

    public MeetingSubscribeMembers saveMemberAtMeeting(Long meetingId, Long userId) {
        MeetingSubscribeMembers meetingSubscribeMembers = meetingSubscribeMembersRedisRepository.findById(meetingId)
                .orElse(new MeetingSubscribeMembers(meetingId));

        meetingSubscribeMembers.addMember(userId);
        meetingSubscribeMembersRedisRepository.save(meetingSubscribeMembers);

        producer.produce(topicProperties.getConnectingMembers(), meetingSubscribeMembers);

        return meetingSubscribeMembers;
    }

    public MeetingSubscribeMembers removeMemberAtMeeting(Long meetingId, Long userId) {
        return removeMember(meetingId, userId);
    }

    private MeetingSubscribeMembers removeMember(Long meetingId, Long userId) {
        MeetingSubscribeMembers meetingSubscribeMembers = meetingSubscribeMembersRedisRepository.findById(meetingId)
                .orElseThrow();

        meetingSubscribeMembers.removeMember(userId);
        if (meetingSubscribeMembers.getUserIds().isEmpty()) {
            meetingSubscribeMembersRedisRepository.delete(meetingSubscribeMembers);
        } else {
            meetingSubscribeMembersRedisRepository.save(meetingSubscribeMembers);
        }

        producer.produce(topicProperties.getConnectingMembers(), meetingSubscribeMembers);
        return meetingSubscribeMembers;
    }

    public void removeMemberAtAllMeetings(List<Long> meetingIds, Long userId) {
        for (Long meetingId : meetingIds) {
            removeMember(meetingId, userId);
        }
    }
}
