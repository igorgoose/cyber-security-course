package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.*;
import by.bsu.kb.schepovpavlovets.server.service.ConnectionService;
import by.bsu.kb.schepovpavlovets.server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/client")
public class ClientController {

    private final ConnectionService connectionService;

    @GetMapping
    public String ok() {
        return "ok";
    }

    @PostMapping ("/signUp")
    public SignedMessageDto<SignUpResponseDto> signUpClient(@RequestBody SignedMessageDto<ClientPublicKeyDto> signedRequest) {
        return connectionService.signUpClient(signedRequest);
    }

    @PostMapping ("/connect")
    public SignedMessageDto<ClientConnectionDto> connect(@RequestBody SignedMessageDto<ConnectRequestDto> signedRequest) {
        return connectionService.createClientConnection(signedRequest);
    }

    @PostMapping ("/disconnect")
    public void disconnect(@RequestBody SignedMessageDto<DisconnectRequestDto> signedRequest) {
        connectionService.destroyClientConnection(signedRequest);
    }
}
