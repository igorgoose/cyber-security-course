package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;

public interface SessionService {

    SessionKeyDto generateSessionKeyWithPublicKey(String publicKey);

    SessionKeyDto generateSessionKeyWithClientId(String clientId);

}
