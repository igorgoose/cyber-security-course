package by.bsu.kb.schepovpavlovets.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/integration")
public class IntegrationController {

    @PostMapping("/session-key")
    public String updateSessionKey() {

    }
}
