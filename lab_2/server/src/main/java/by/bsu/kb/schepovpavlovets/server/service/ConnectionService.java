package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.*;

import javax.transaction.Transactional;

public interface ConnectionService {

    SignedMessageDto<SignUpResponseDto> signUpClient(SignedMessageDto<ClientPublicKeyDto> publicKey);

    SignedMessageDto<ClientConnectionDto> createClientConnection(SignedMessageDto<ConnectRequestDto> signedRequest);

    @Transactional
    void destroyClientConnection(SignedMessageDto<DisconnectRequestDto> signedRequest);
}
