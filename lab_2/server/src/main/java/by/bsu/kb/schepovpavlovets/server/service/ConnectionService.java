package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.ClientConnectionDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignUpResponseDto;

import javax.transaction.Transactional;

public interface ConnectionService {

    SignUpResponseDto signUpClient(String publicKey);

    ClientConnectionDto createClientConnection(String encodedClientId, String encodedNamespace);

    @Transactional
    void destroyClientConnection(String encodedClientId, String encodedConnectionId);
}
