package com.comeon.websocket.config;

import java.security.Principal;

public interface UserPrincipalProvider {

    Principal createUserPrincipalByToken(String token);
}
