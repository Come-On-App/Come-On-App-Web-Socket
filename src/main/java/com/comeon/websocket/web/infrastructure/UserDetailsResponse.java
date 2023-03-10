package com.comeon.websocket.web.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String email;
    private String name;
}
