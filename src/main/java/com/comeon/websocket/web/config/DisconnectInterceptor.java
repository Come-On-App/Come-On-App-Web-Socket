package com.comeon.websocket.web.config;

import com.comeon.websocket.config.kafka.KafkaTopicProperties;
import com.comeon.websocket.config.kafka.producer.KafkaProducer;
import com.comeon.websocket.utils.StompSessionAttrUtils;
import com.comeon.websocket.web.message.dto.MeetingSubUnsubKafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectInterceptor implements ChannelInterceptor {

    private static final String MEETING_SUBSCRIBE_PATH_PATTERN = "/sub/meetings/{meetingId}";

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final MeetingSubscriptionRepository meetingSubscriptionRepository;
    private final KafkaTopicProperties topicProperties;
    private final KafkaProducer producer;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.DISCONNECT.equals(headerAccessor.getCommand())) {
            log.debug("session {} disconnect.....", headerAccessor.getSessionId());
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            Map<String, String> subscriptions = (Map<String, String>) sessionAttributes.get("subscriptions");
            if (subscriptions == null) return null;

            for (String subscriptionId : subscriptions.keySet()) {
                String destination = subscriptions.get(subscriptionId);

                if (pathMatcher.match(MEETING_SUBSCRIBE_PATH_PATTERN, destination)) {
                    Long meetingId = parseMeetingIdFromDestination(destination);
                    Long userId = StompSessionAttrUtils.getUserId(sessionAttributes);

                    boolean result = meetingSubscriptionRepository.removeSession(meetingId, userId, headerAccessor.getSessionId(), subscriptionId);
                    if (result) {
                        MeetingSubUnsubKafkaMessage kafkaMessage = MeetingSubUnsubKafkaMessage.createUnsubMessage(
                                meetingId,
                                userId,
                                meetingSubscriptionRepository.getUsersAtMeeting(meetingId)
                        );
                        producer.produce(topicProperties.getConnectingMembers(), kafkaMessage);
                        log.info("member(at meeting-id: {}, user-id: {}) unsubscribed..", meetingId, userId);
                    }

                    subscriptions.remove(headerAccessor.getSubscriptionId());
                }
            }
        }

        return message;
    }

    private Long parseMeetingIdFromDestination(String destination) {
        if (pathMatcher.match(MEETING_SUBSCRIBE_PATH_PATTERN, destination)) {
            Map<String, String> meetingIdMap = pathMatcher.extractUriTemplateVariables(MEETING_SUBSCRIBE_PATH_PATTERN, destination);
            String meetingIdStr = meetingIdMap.getOrDefault("meetingId", null);
            if (!StringUtils.hasText(meetingIdStr)) {
                throw new RuntimeException("no meeting id");
            }

            Long meetingId = null;
            try {
                meetingId = Long.parseLong(meetingIdStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("meeting id must be number", e);
            }
            return meetingId;
        }

        return null;
    }
}
