package com.comeon.websocket.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtParser {

    private final ObjectMapper objectMapper;

    public Long parseUserId(String token) {
        String[] split = token.split("\\.");
        String payloadString = decodePayload(split[1]);
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(payloadString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("토큰 정보를 읽을 수 없습니다.");
        }

        return Long.parseLong(payload.get("uid").toString());
    }

    private String decodePayload(String jwtPayload) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(jwtPayload));
    }
}
