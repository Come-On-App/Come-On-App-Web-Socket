package com.comeon.websocket.web.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResponse {

    private Long memberId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String memberRole;
}
