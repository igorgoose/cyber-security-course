package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/client")
public class ClientController {

    @PostMapping ("/session")
    public SessionKeyDto generateSessionKey(@RequestParam(value = "public-key", required = false) String publicKey,
            @RequestParam("clientId") String clientId) {
        return null;
    }
}
