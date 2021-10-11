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
    private final FileService fileService;

    @GetMapping
    public String ok() {
        return "ok";
    }

    @PostMapping ("/signUp")
    public SignUpResponseDto signUpClient(@RequestBody ClientPublicKeyDto clientPublicKeyDto) {
        return connectionService.signUpClient(clientPublicKeyDto.getBase64Key());
    }

    @PostMapping ("/connect")
    public ClientConnectionDto connect(@RequestBody ConnectRequestDto connectRequestDto) {
        return connectionService.createClientConnection(connectRequestDto.getEncodedClientId(), connectRequestDto.getEncodedNamespace());
    }

    @PostMapping ("/disconnect")
    public void disconnect(@RequestBody DisconnectRequestDto disconnectRequestDto) {
        connectionService.destroyClientConnection(disconnectRequestDto.getEncodedClientId(), disconnectRequestDto.getEncodedConnectionId());
    }
}
