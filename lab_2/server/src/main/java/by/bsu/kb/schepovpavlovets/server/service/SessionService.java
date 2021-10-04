package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignUpResponseDto;

public interface SessionService {

    SignUpResponseDto signUpClient(String publicKey);

    SessionKeyDto generateSessionKeyWithClientId(String clientId);

}
