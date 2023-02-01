package com.comeon.websocket.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {

    @GetMapping("/test/ws")
    public String testPage() {
        return "stomp-test-page";
    }
}
