package com.comeon.websocket.web.config;

import com.comeon.websocket.utils.StompSessionAttrUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.*;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.broker.AbstractSubscriptionRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

// TODO Refactoring
@Slf4j
@Component
public class CustomSubscriptionRegistry extends AbstractSubscriptionRegistry {

    private static final String MEETING_SUBSCRIBE_PATH_PATTERN = "/sub/meetings/{meetingId}";
    private static final String MEETING_QUEUE_PATH_PATTERN = "/queue/meetings/{meetingIdWithSessionId}";
    public static final int DEFAULT_CACHE_LIMIT = 1024;

    private static final EvaluationContext messageEvalContext =
            SimpleEvaluationContext.forPropertyAccessors(new SimpMessageHeaderPropertyAccessor()).build();

    private PathMatcher pathMatcher = new AntPathMatcher();
    private int cacheLimit = DEFAULT_CACHE_LIMIT;

    @Nullable
    private String selectorHeaderName = "selector";

    private volatile boolean selectorHeaderInUse;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final DestinationCache destinationCache = new DestinationCache();
    private final SessionRegistry sessionRegistry = new SessionRegistry();

    // DI
    private final MeetingMemberInfoProvider meetingMemberInfoProvider;
    private final MeetingSubscribeMemberRepository meetingSubscribeMemberRepository;

    public CustomSubscriptionRegistry(MeetingMemberInfoProvider meetingMemberInfoProvider,
                                      MeetingSubscribeMemberRepository meetingSubscribeMemberRepository
    ) {
        this.meetingMemberInfoProvider = meetingMemberInfoProvider;
        this.meetingSubscribeMemberRepository = meetingSubscribeMemberRepository;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
        this.destinationCache.ensureCacheLimit();
    }

    public int getCacheLimit() {
        return this.cacheLimit;
    }

    public void setSelectorHeaderName(@Nullable String selectorHeaderName) {
        this.selectorHeaderName = (StringUtils.hasText(selectorHeaderName) ? selectorHeaderName : null);
    }

    @Nullable
    public String getSelectorHeaderName() {
        return this.selectorHeaderName;
    }

    // 구독 요청시 호출
    @Override
    protected void addSubscriptionInternal(String sessionId, String subscriptionId, String destination, Message<?> message) {
        log.debug("{} - START", getMethodName());
        log.debug("destination: {}", destination);

        // destination 에서 meetingId parsing
        Long meetingId = parseMeetingIdFromDestination(destination);

        // 토큰으로 모임 및 유저 권한 확인
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String token = StompSessionAttrUtils.getToken(sessionAttributes);
        MeetingMemberInfo memberInfo = meetingMemberInfoProvider.getMeetingMemberInfoBy(token, meetingId);

        boolean isPattern = this.pathMatcher.isPattern(destination);
        Expression expression = getSelectorExpression(message.getHeaders());
        Subscription subscription = new Subscription(subscriptionId, destination, isPattern, expression);

        // 레디스 저장 및 카프카 적재
        if (pathMatcher.match(MEETING_SUBSCRIBE_PATH_PATTERN, destination)) {
            meetingSubscribeMemberRepository.saveMemberAtMeeting(memberInfo.getMeetingId(), sessionId, memberInfo.getUserId());
        }

        // 세션 저장소에 subscription 정보 저장
        this.sessionRegistry.addSubscription(sessionId, subscription, memberInfo.getUserId());
        // subscription.destination 캐시에 subscription-id 저장
        this.destinationCache.updateAfterNewSubscription(sessionId, subscription);

        logSessionRegistry();
        logDestinationCache();
        log.debug("{} - END", getMethodName());
    }

    private Long parseMeetingIdFromDestination(String destination) {
        if (pathMatcher.match(MEETING_QUEUE_PATH_PATTERN, destination)) {
            Map<String, String> meetingIdMap = pathMatcher.extractUriTemplateVariables(MEETING_QUEUE_PATH_PATTERN, destination);
            String meetingIdWithSessionId = meetingIdMap.getOrDefault("meetingIdWithSessionId", null);
            String meetingIdStr = meetingIdWithSessionId.split("-")[0];
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

    @Nullable
    private Expression getSelectorExpression(MessageHeaders headers) {
        if (getSelectorHeaderName() == null) {
            return null;
        }
        String selector = SimpMessageHeaderAccessor.getFirstNativeHeader(getSelectorHeaderName(), headers);
        if (selector == null) {
            return null;
        }
        Expression expression = null;
        try {
            expression = this.expressionParser.parseExpression(selector);
            this.selectorHeaderInUse = true;
            if (logger.isTraceEnabled()) {
                logger.trace("Subscription selector: [" + selector + "]");
            }
        } catch (Throwable ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to parse selector: " + selector, ex);
            }
        }
        return expression;
    }

    // 구독 해제시 호출
    @Override
    protected void removeSubscriptionInternal(String sessionId, String subscriptionId, Message<?> message) {
        log.debug("{} - START", getMethodName());
        log.debug("subscriptionId: {}", subscriptionId);
        SessionInfo info = this.sessionRegistry.getSession(sessionId);
        if (info == null || info.getSubscription(subscriptionId) == null) {
            return;
        }

        Subscription subscription = info.removeSubscription(subscriptionId);
        if (subscription != null) {
            if (pathMatcher.match(MEETING_SUBSCRIBE_PATH_PATTERN, subscription.destination)) {
                StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
                Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
                Long userId = StompSessionAttrUtils.getUserId(sessionAttributes);
                Long meetingId = parseMeetingIdFromDestination(subscription.destination);

                meetingSubscribeMemberRepository.removeMemberAtMeeting(meetingId, userId, sessionId);
                log.debug("sessionId: {}, subscriptionId: {}, destination: {} removed", sessionId, subscriptionId, subscription.destination);
            }

            this.destinationCache.updateAfterRemovedSubscription(sessionId, subscription);

            logSessionRegistry();
            logDestinationCache();
        }

        log.debug("{} - END", getMethodName());
    }

    // 웹소켓 커넥션 종료시 호출
    @Override
    public void unregisterAllSubscriptions(String sessionId) {
        log.debug("{} - START", getMethodName());

        SessionInfo info = this.sessionRegistry.removeSubscriptions(sessionId);
        if (info != null) {
            log.debug("userId: {}, sessionId: {}", info.getUserId(), sessionId);
            List<Long> meetingIds = new ArrayList<>();
            info.getSubscriptions().forEach(
                    subscription -> {
                        try {
                            if (pathMatcher.match(MEETING_SUBSCRIBE_PATH_PATTERN, subscription.destination)) {
                                Long meetingId = parseMeetingIdFromDestination(subscription.destination);
                                meetingIds.add(meetingId);
                            }
                        } catch (Exception e) {
                        }
                    }
            );
            meetingSubscribeMemberRepository.removeMemberAtAllMeetings(meetingIds, info.userId, sessionId);

            this.destinationCache.updateAfterRemovedSession(sessionId, info);
        }

        logSessionRegistry();
        logDestinationCache();
        log.debug("{} - END", getMethodName());
    }

    @Override
    protected MultiValueMap<String, String> findSubscriptionsInternal(String destination, Message<?> message) {
        log.debug("{} - START", getMethodName());
        log.debug("destination: {}", destination);

        MultiValueMap<String, String> allMatches = this.destinationCache.getSubscriptions(destination);
        if (!this.selectorHeaderInUse) {
            log.debug("this.selectorHeaderInUse = false");
            logSessionRegistry();
            logDestinationCache();
            log.debug("{} - END", getMethodName());
            return allMatches;
        }

        MultiValueMap<String, String> result = new LinkedMultiValueMap<>(allMatches.size());
        log.debug("allMatches.size() : {}", allMatches.size());
        allMatches.forEach((sessionId, subscriptionIds) -> {
            SessionInfo info = this.sessionRegistry.getSession(sessionId);
            if (info != null) {
                for (String subscriptionId : subscriptionIds) {
                    Subscription subscription = info.getSubscription(subscriptionId);
                    if (subscription != null && evaluateExpression(subscription.getSelector(), message)) {
                        result.add(sessionId, subscription.getId());
                    }
                }
            }
        });

        logSessionRegistry();
        logDestinationCache();
        log.debug("{} - END", getMethodName());
        return result;
    }

    private boolean evaluateExpression(@Nullable Expression expression, Message<?> message) {
        if (expression == null) {
            return true;
        }
        try {
            Boolean result = expression.getValue(messageEvalContext, message, Boolean.class);
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        } catch (SpelEvaluationException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to evaluate selector: " + ex.getMessage());
            }
        } catch (Throwable ex) {
            logger.debug("Failed to evaluate selector", ex);
        }
        return false;
    }

    public void logSessionRegistry() {
        log.debug("sessionRegistry.sessions: {}", sessionRegistry.sessions);
    }

    public void logDestinationCache() {
        log.debug("destinationCache.cacheMap: {}", destinationCache.destinationCache);
    }

    private final class DestinationCache {

        // destination -> [sessionId -> subscriptionId's]
        private final Map<String, LinkedMultiValueMap<String, String>> destinationCache =
                new ConcurrentHashMap<>(DEFAULT_CACHE_LIMIT);

        private final AtomicInteger cacheSize = new AtomicInteger();

        private final Queue<String> cacheEvictionPolicy = new ConcurrentLinkedQueue<>();

        public LinkedMultiValueMap<String, String> getSubscriptions(String destination) {
            LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds = this.destinationCache.get(destination);
            if (sessionIdToSubscriptionIds == null) {
                sessionIdToSubscriptionIds = this.destinationCache.computeIfAbsent(destination, _destination -> {
                    LinkedMultiValueMap<String, String> matches = computeMatchingSubscriptions(destination);
                    // Update queue first, so that cacheSize <= queue.size(
                    this.cacheEvictionPolicy.add(destination);
                    this.cacheSize.incrementAndGet();
                    return matches;
                });
                ensureCacheLimit();
            }
            return sessionIdToSubscriptionIds;
        }

        private LinkedMultiValueMap<String, String> computeMatchingSubscriptions(String destination) {
            log.debug("{} - START", getMethodName());

            LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds = new LinkedMultiValueMap<>();
            CustomSubscriptionRegistry.this.sessionRegistry.forEachSubscription((sessionId, subscription) -> {
                if (subscription.isPattern()) {
                    log.debug("subscription.isPattern() = true");
                    if (pathMatcher.match(subscription.getDestination(), destination)) {
                        log.debug("pathMatcher.match(subscription.getDestination(), destination) = true");
                        addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscription.getId());
                    }
                } else if (destination.equals(subscription.getDestination())) {
                    log.debug("destination.equals(subscription.getDestination()) = true");
                    log.debug("destination: {}", destination);
                    addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscription.getId());
                }
            });

            log.debug("{} - END", getMethodName());
            return sessionIdToSubscriptionIds;
        }

        // subscription 조회시, subscription 새로 등록시 이용
        // destination에 매핑됨
        private void addMatchedSubscriptionId(
                LinkedMultiValueMap<String, String> sessionIdToSubscriptionIds,
                String sessionId, String subscriptionId
        ) {
            sessionIdToSubscriptionIds.compute(sessionId, (_sessionId, subscriptionIds) -> {
                // sessionId에 해당하는 값이 없으면 리스트 생성
                if (subscriptionIds == null) {
                    log.debug("Save sessionId [{}] with subscriptionId [{}] at destinationCache", sessionId, subscriptionId);
                    return Collections.singletonList(subscriptionId);
                }
                // sessionId에 해당하는 값이 있으면 그냥 리턴
                else {
                    log.warn("subscriptionId [{}] at sessionId [{}] is already exist at destinationCache...", subscriptionIds.get(0), sessionId);
                    return subscriptionIds;
                }
            });
        }

        private void ensureCacheLimit() {
            int size = this.cacheSize.get();
            if (size > cacheLimit) {
                do {
                    if (this.cacheSize.compareAndSet(size, size - 1)) {
                        // Remove (vs poll): we expect an element
                        String head = this.cacheEvictionPolicy.remove();
                        this.destinationCache.remove(head);
                    }
                } while ((size = this.cacheSize.get()) > cacheLimit);
            }
        }

        public void updateAfterNewSubscription(String sessionId, Subscription subscription) {
            if (subscription.isPattern()) {
                for (String cachedDestination : this.destinationCache.keySet()) {
                    if (pathMatcher.match(subscription.getDestination(), cachedDestination)) {
                        addToDestination(cachedDestination, sessionId, subscription.getId());
                    }
                }
            } else {
                addToDestination(subscription.getDestination(), sessionId, subscription.getId());
            }
        }

        private void addToDestination(String destination, String sessionId, String subscriptionId) {
            this.destinationCache.computeIfPresent(destination, (_destination, sessionIdToSubscriptionIds) -> {
                sessionIdToSubscriptionIds = sessionIdToSubscriptionIds.clone();
                addMatchedSubscriptionId(sessionIdToSubscriptionIds, sessionId, subscriptionId);
                return sessionIdToSubscriptionIds;
            });
        }

        public void updateAfterRemovedSubscription(String sessionId, Subscription subscription) {
            if (subscription.isPattern()) {
                String subscriptionId = subscription.getId();
                this.destinationCache.forEach((destination, sessionIdToSubscriptionIds) -> {
                    List<String> subscriptionIds = sessionIdToSubscriptionIds.get(sessionId);
                    if (subscriptionIds != null && subscriptionIds.contains(subscriptionId)) {
                        removeInternal(destination, sessionId, subscriptionId);
                    }
                });
            } else {
                removeInternal(subscription.getDestination(), sessionId, subscription.getId());
            }
        }

        private void removeInternal(String destination, String sessionId, String subscriptionId) {
            this.destinationCache.computeIfPresent(destination, (_destination, sessionIdToSubscriptionIds) -> {
                sessionIdToSubscriptionIds = sessionIdToSubscriptionIds.clone();
                sessionIdToSubscriptionIds.computeIfPresent(sessionId, (_sessionId, subscriptionIds) -> {
                    /* Most likely case: single subscription per destination per session. */
                    if (subscriptionIds.size() == 1 && subscriptionId.equals(subscriptionIds.get(0))) {
                        return null;
                    }
                    subscriptionIds = new ArrayList<>(subscriptionIds);
                    subscriptionIds.remove(subscriptionId);
                    return (subscriptionIds.isEmpty() ? null : subscriptionIds);
                });
                return sessionIdToSubscriptionIds;
            });

            LinkedMultiValueMap<String, String> destinationCacheMap = this.destinationCache.getOrDefault(destination, null);
            if (destinationCacheMap != null && destinationCacheMap.isEmpty()) {
                this.destinationCache.remove(destination);
            }
        }

        public void updateAfterRemovedSession(String sessionId, SessionInfo info) {
            for (Subscription subscription : info.getSubscriptions()) {
                updateAfterRemovedSubscription(sessionId, subscription);
            }
        }
    }

    private static final class SessionRegistry {

        // <sessionId(string), sessionInfo(object)>
        private final ConcurrentMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

        @Nullable
        public SessionInfo getSession(String sessionId) {
            return this.sessions.get(sessionId);
        }

        public void forEachSubscription(BiConsumer<String, Subscription> consumer) {
            this.sessions.forEach((sessionId, info) ->
                    info.getSubscriptions().forEach(subscription -> consumer.accept(sessionId, subscription)));
        }

        public void addSubscription(String sessionId, Subscription subscription, Long userId) {
            SessionInfo info = this.sessions.computeIfAbsent(sessionId, _sessionId -> new SessionInfo(userId));
            info.addSubscription(subscription);
        }

        @Nullable
        public SessionInfo removeSubscriptions(String sessionId) {
            return this.sessions.remove(sessionId);
        }
    }

    private static final class SessionInfo {

        private final Long userId;
        // <subscriptionId, Subscription>
        private final Map<String, Subscription> subscriptionMap;

        private SessionInfo(Long userId) {
            this.userId = userId;
            this.subscriptionMap = new ConcurrentHashMap<>();
        }

        private SessionInfo(Long userId, Map<String, Subscription> subscriptionMap) {
            this.userId = userId;
            this.subscriptionMap = subscriptionMap;
        }

        public Long getUserId() {
            return userId;
        }

        public Collection<Subscription> getSubscriptions() {
            return this.subscriptionMap.values();
        }

        @Nullable
        public Subscription getSubscription(String subscriptionId) {
            return this.subscriptionMap.get(subscriptionId);
        }

        public void addSubscription(Subscription subscription) {
            // 세션에 요청한 subscription의 destination과 동일한 subscription이 존재하는지 탐색
            Optional<Subscription> subscriptionOpt = this.subscriptionMap.values().stream()
                    .filter(sub -> sub.destination.equals(subscription.destination))
                    .findFirst();

            if (subscriptionOpt.isPresent()) {
                Subscription exist = subscriptionOpt.get();
                log.warn("destination [{}] is already exist at sessionRegistry... id of exist subscription: [{}]", exist.destination, exist.id);
                return;
            }
            // 존재하지 않으면 추가
            this.subscriptionMap.putIfAbsent(subscription.getId(), subscription);
        }

        @Nullable
        public Subscription removeSubscription(String subscriptionId) {
            return this.subscriptionMap.remove(subscriptionId);
        }

        @Override
        public String toString() {
            return this.subscriptionMap.toString();
        }
    }

    private static final class Subscription {

        private final String id;

        private final String destination;

        private final boolean isPattern;

        @Nullable
        private final Expression selector;

        public Subscription(String id, String destination, boolean isPattern, @Nullable Expression selector) {
            Assert.notNull(id, "Subscription id must not be null");
            Assert.notNull(destination, "Subscription destination must not be null");
            this.id = id;
            this.selector = selector;
            this.destination = destination;
            this.isPattern = isPattern;
        }

        public String getId() {
            return this.id;
        }

        public String getDestination() {
            return this.destination;
        }

        public boolean isPattern() {
            return this.isPattern;
        }

        @Nullable
        public Expression getSelector() {
            return this.selector;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other ||
                    (other instanceof Subscription && this.id.equals(((Subscription) other).id)));
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        @Override
        public String toString() {
            return "subscription(id=" + this.id + ", destination=" + destination + ")";
        }
    }


    private static class SimpMessageHeaderPropertyAccessor implements PropertyAccessor {

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class<?>[]{Message.class, MessageHeaders.class};
        }

        @Override
        public boolean canRead(EvaluationContext context, @Nullable Object target, String name) {
            return true;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public TypedValue read(EvaluationContext context, @Nullable Object target, String name) {
            Object value;
            if (target instanceof Message) {
                value = name.equals("headers") ? ((Message) target).getHeaders() : null;
            } else if (target instanceof MessageHeaders) {
                MessageHeaders headers = (MessageHeaders) target;
                SimpMessageHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(headers, SimpMessageHeaderAccessor.class);
                Assert.state(accessor != null, "No SimpMessageHeaderAccessor");
                if ("destination".equalsIgnoreCase(name)) {
                    value = accessor.getDestination();
                } else {
                    value = accessor.getFirstNativeHeader(name);
                    if (value == null) {
                        value = headers.get(name);
                    }
                }
            } else {
                // Should never happen...
                throw new IllegalStateException("Expected Message or MessageHeaders.");
            }
            return new TypedValue(value);
        }

        @Override
        public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object value) {
        }

    }

    private static String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}
