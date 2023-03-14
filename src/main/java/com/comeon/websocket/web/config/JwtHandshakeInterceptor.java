package com.comeon.websocket.web.config;

import com.comeon.websocket.utils.StompSessionAttrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final UserInfoProvider userInfoProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        Principal principal = request.getPrincipal();
        if (principal != null) {
            log.debug("request.principal: {}", principal.getName());
        }
        if (request instanceof ServletServerHttpRequest) {
            String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");
            if (StringUtils.hasText(token)) {
                Long userId = userInfoProvider.getUserIdBy(token);
                if (userId != null) {
                    StompSessionAttrUtils.setUserId(attributes, userId);
                    StompSessionAttrUtils.setToken(attributes, token);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
