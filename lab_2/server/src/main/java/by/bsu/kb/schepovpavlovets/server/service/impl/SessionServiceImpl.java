package by.bsu.kb.schepovpavlovets.server.service.impl;

import by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException;
import by.bsu.kb.schepovpavlovets.server.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignUpResponseDto;
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
import java.util.UUID;

import static by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException.Status.INVALID_CLIENT_ID;

@RequiredArgsConstructor
@Service
public class SessionServiceImpl implements SessionService {

    private final ClientRepository clientRepository;
    private final CryptUtility cryptUtility;

    @Transactional
    @Override
    public SignUpResponseDto signUpClient(String publicKey) {
        Client client = new Client();
        LocalDateTime sessionExpiresAt = LocalDateTime.now().plus(10, ChronoUnit.MINUTES);
        byte[] sessionBytes = cryptUtility.generateRandomBytes(32);
        String session = Base64.encodeBase64String(sessionBytes);
        client.setSession(session);
        client.setSessionExpiresAt(sessionExpiresAt);
        client.setDisabled(false);
        clientRepository.save(client);
        byte[] publicKeyBytes = Base64.decodeBase64(publicKey);
        cryptUtility.saveClientPublicKey(publicKeyBytes, client.getId().toString());
        return SignUpResponseDto.builder()
                                .publicKey(Base64.encodeBase64String(cryptUtility.getPublicKeyEncoded())) // base64 server public key
                                .clientId(cryptUtility.encodeBytesBase64RSA(sessionBytes, client.getId().toString())) // session bytes to base64
                                .sessionKey(cryptUtility.encodeStringBase64RSA(client.getId().toString(), client.getId().toString())) // clientId base64
                                .build();
    }

    @Transactional
    @Override
    public SessionKeyDto generateSessionKeyWithClientId(String encodedClientId) {
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(encodedClientId));
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        byte[] sessionBytes = cryptUtility.generateRandomBytes(32);
        String session = Base64.encodeBase64String(sessionBytes);
        LocalDateTime sessionExpiresAt = LocalDateTime.now().plus(10, ChronoUnit.MINUTES);
        client.setSession(session);
        client.setSessionExpiresAt(sessionExpiresAt);
        clientRepository.save(client);
        return SessionKeyDto.builder()
                            .sessionKey(cryptUtility.encodeStringBase64RSA(client.getId().toString(), client.getId().toString()))
                            .build();
    }

    @Transactional
    @Override
    public void invalidateSession(String encodedClientId) {
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(encodedClientId));
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        client.setDisabled(true);
        clientRepository.save(client);
    }

}
