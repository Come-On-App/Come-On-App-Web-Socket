package com.comeon.websocket.web.infrastructure;

import com.comeon.websocket.web.config.MeetingSubscribeMembers;
import org.springframework.data.repository.CrudRepository;

public interface MeetingSubscribeMembersRedisRepository extends CrudRepository<MeetingSubscribeMembers, Long> {
}
