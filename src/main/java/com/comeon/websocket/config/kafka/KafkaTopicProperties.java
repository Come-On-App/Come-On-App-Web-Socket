package com.comeon.websocket.config.kafka;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KafkaTopicProperties {

    @Value("${kafka.topic.connecting-members}")
    private String connectingMembers;

    @Value("${kafka.topic.meeting-meta-data}")
    private String meetingMetaData;

    @Value("${kafka.topic.meeting-members}")
    private String meetingMembers;

    @Value("${kafka.topic.meeting-places}")
    private String meetingPlaces;

    @Value("${kafka.topic.meeting-voting}")
    private String meetingVoting;

    @Value("${kafka.topic.meeting-fixed-date}")
    private String meetingFixedDate;

    @Value("${kafka.topic.meeting-time}")
    private String meetingTime;
}
