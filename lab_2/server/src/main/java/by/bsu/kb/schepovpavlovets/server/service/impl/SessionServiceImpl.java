package by.bsu.kb.schepovpavlovets.server.service.impl;

import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.entity.Client;
import by.bsu.kb.schepovpavlovets.server.repository.ClientRepository;
import by.bsu.kb.schepovpavlovets.server.service.SessionService;
import by.bsu.kb.schepovpavlovets.server.util.CryptUtility;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {

    private final ClientRepository clientRepository;
    private final CryptUtility cryptUtility;

    @Transactional
    @Override
    public SessionKeyDto generateSessionKeyWithPublicKey(String publicKey) {
        Client client = new Client();
        LocalDateTime sessionExpiresAt = LocalDateTime.now().plus(10, ChronoUnit.MINUTES);
        String session = Base64.encodeBase64String(cryptUtility.generateRandomBytes(32));
        client.setSession(session);
        client.setSessionExpiresAt(sessionExpiresAt);
        clientRepository.save(client);
        byte[] publicKeyBytes = Base64.decodeBase64(publicKey);
        cryptUtility.savePublicKey(publicKeyBytes, client.getId().toString());
        return SessionKeyDto.builder()
                            .clientId(cryptUtility.encodeStringBase64(session, client.getId().toString()))
                            .key(cryptUtility.encodeStringBase64(client.getId().toString(), client.getId().toString()))
                            .build();
    }

    @Override
    public SessionKeyDto generateSessionKeyWithClientId(String clientId) {
        
        return null;
    }

}
