package com.comeon.websocket.web.message.controller;

import com.comeon.websocket.web.message.dto.MeetingDateVotingResourceMessage;
import com.comeon.websocket.web.message.dto.StompMessage;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class MeetingDateVotingResourceConsumer extends AbstractMessageConsumer {

    public MeetingDateVotingResourceConsumer(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @KafkaListener(id = "date-voting", topics = {"${kafka.topic.meeting-voting}"})
    public void consume(@Payload VotingListUpdateMessage payload) {
        Long meetingId = payload.getMeetingId();
        StompMessage<MeetingDateVotingResourceMessage> stompMessage =
                StompMessage.resourceUpdated(MeetingDateVotingResourceMessage.create(meetingId, payload.getDate()));

        messagingTemplate.setHeaderInitializer(
                headerAccessor -> headerAccessor.setContentType(MediaType.APPLICATION_JSON)
        );

        String destination = "/sub/meetings/" + meetingId;
        messagingTemplate.convertAndSend(destination, stompMessage);
        log.info("[meeting-date-votings-update] send to meeting -> destination: {}", destination);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotingListUpdateMessage {
        private Long meetingId;

        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate date;
    }
}
