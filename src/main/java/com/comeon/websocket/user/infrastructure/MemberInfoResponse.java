package com.comeon.websocket.user.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResponse {

    private Long meetingId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String meetingRole;
}
