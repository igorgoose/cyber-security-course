package by.bsu.kb.schepovpavlovets.server.service.impl;

import by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException;
import by.bsu.kb.schepovpavlovets.server.model.dto.*;
import by.bsu.kb.schepovpavlovets.server.model.entity.Client;
import by.bsu.kb.schepovpavlovets.server.model.entity.ClientConnection;
import by.bsu.kb.schepovpavlovets.server.repository.ClientConnectionRepository;
import by.bsu.kb.schepovpavlovets.server.repository.ClientRepository;
import by.bsu.kb.schepovpavlovets.server.service.FileService;
import by.bsu.kb.schepovpavlovets.server.service.ConnectionService;
import by.bsu.kb.schepovpavlovets.server.util.CryptUtility;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException.Status.INVALID_CLIENT_ID;
import static by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException.Status.UNKNOWN_CONNECTION;

@RequiredArgsConstructor
@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final ClientRepository clientRepository;
    private final ClientConnectionRepository clientConnectionRepository;
    private final CryptUtility cryptUtility;
    private final FileService fileService;
    private final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    @Override
    public SignedMessageDto<SignUpResponseDto> signUpClient(SignedMessageDto<ClientPublicKeyDto> signedRequest) {
        cryptUtility.verifySignature(signedRequest, signedRequest.getContent().getBase64Key());
        Client client = new Client();
        client.setDisabled(false);
        clientRepository.save(client);
        byte[] publicKeyBytes = Base64.decodeBase64(signedRequest.getContent().getBase64Key());
        byte[] publicKeyRSABytes = Base64.decodeBase64(signedRequest.getContent().getBase64KeyRsa());
        cryptUtility.saveClientPublicKey(publicKeyBytes, client.getId().toString());
        cryptUtility.saveClientPublicKeyRSA(publicKeyRSABytes, client.getId().toString());
        fileService.createClientFolder(client.getId().toString());
        return cryptUtility.signResponse(SignUpResponseDto.builder()
                                .publicKey(Base64.encodeBase64String(cryptUtility.getPublicKeyEncoded()))
                                .clientId(client.getId().toString())
                                .build());
    }

    @Transactional
    @Override
    public SignedMessageDto<ClientConnectionDto> createClientConnection(SignedMessageDto<ConnectRequestDto> signedRequest) {
        String clientId = signedRequest.getContent().getEncodedClientId();
        String namespace = signedRequest.getContent().getEncodedNamespace();
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        cryptUtility.verifySignature(signedRequest, client.getId());
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.setClient(client);
        byte[] sessionBytes = cryptUtility.generateRandomBytes(32);
        byte[] ivBytes = cryptUtility.generateRandomBytes(16);
        String session = Base64.encodeBase64String(sessionBytes);
        LocalDateTime expiresAt = LocalDateTime.now().plus(10, ChronoUnit.MINUTES);
        String iv = Base64.encodeBase64String(ivBytes);
        clientConnection.setSession(session);
        clientConnection.setIv(iv);
        clientConnection.setExpiresAt(expiresAt.plus(1, ChronoUnit.SECONDS));
        clientConnectionRepository.save(clientConnection);
        fileService.createNamespace(clientId, namespace);
        //todo think of encryption
        return cryptUtility.signResponse(ClientConnectionDto.builder()
                                  .connectionId(clientConnection.getId().toString())
                                  .sessionKey(cryptUtility.encodeBytesBase64RSA(sessionBytes, client.getId().toString()))
                                  .iv(cryptUtility.encodeBytesBase64RSA(ivBytes, client.getId().toString()))
                                  .expiresAt(dtf.format(expiresAt))
                                  .build());
    }

    @Transactional
    @Override
    public void destroyClientConnection(SignedMessageDto<DisconnectRequestDto> signedRequest) {
        String clientId = signedRequest.getContent().getEncodedClientId();
        String connectionId = signedRequest.getContent().getEncodedConnectionId();
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        cryptUtility.verifySignature(signedRequest, client.getId());
        if (!clientConnectionRepository.existsByIdAndClientId(UUID.fromString(connectionId), client.getId())) {
            throw new UnauthorizedException(UNKNOWN_CONNECTION.name());
        }
        clientConnectionRepository.deleteById(UUID.fromString(connectionId));
    }

}
