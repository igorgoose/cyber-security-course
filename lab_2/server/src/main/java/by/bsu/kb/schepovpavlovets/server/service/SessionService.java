package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignUpResponseDto;

import javax.transaction.Transactional;

public interface SessionService {

    SignUpResponseDto signUpClient(String publicKey);

    SessionKeyDto generateSessionKeyWithClientId(String clientId);

    @Transactional
    void invalidateSession(String encodedClientId);
}
