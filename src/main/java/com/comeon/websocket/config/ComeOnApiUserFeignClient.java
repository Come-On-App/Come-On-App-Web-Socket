package com.comeon.websocket.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "come-on-api-user-feign-client",
        url = "${comeon.api-server.url}"
)
public interface ComeOnApiUserFeignClient {

    @GetMapping(value = "/api/v1/users/me")
    UserDetailsResponse getUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}
