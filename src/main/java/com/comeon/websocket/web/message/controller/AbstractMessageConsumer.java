package com.comeon.websocket.web.message.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;

public abstract class AbstractMessageConsumer {

    protected final SimpMessagingTemplate messagingTemplate;

    public AbstractMessageConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
}
