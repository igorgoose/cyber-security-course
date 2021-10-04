package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.ClientPublicKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SessionUpdateRequestDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignUpResponseDto;
import by.bsu.kb.schepovpavlovets.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/client")
public class ClientController {

    private final SessionService sessionService;

    @PostMapping ("/signUp")
    public SignUpResponseDto signUpClient(@RequestBody ClientPublicKeyDto clientPublicKeyDto) {
        return sessionService.signUpClient(clientPublicKeyDto.getBase64Key());
    }

    @PostMapping ("/session")
    public SessionKeyDto generateSession(@RequestBody SessionUpdateRequestDto sessionUpdateRequestDto) {
        return sessionService.generateSessionKeyWithClientId(sessionUpdateRequestDto.getEncodedClientId());
    }
}
