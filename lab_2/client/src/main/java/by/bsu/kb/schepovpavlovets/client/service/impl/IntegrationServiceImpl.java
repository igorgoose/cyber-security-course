package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.NoServerException;
import by.bsu.kb.schepovpavlovets.client.model.dto.PublicKeyDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerSignUpResponseDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.SessionKeyDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.SessionUpdateRequestDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.repository.ServerDataRepository;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.util.CryptUtility;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static by.bsu.kb.schepovpavlovets.client.model.entity.ServerData.ConnectionStatus.*;

@RequiredArgsConstructor
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private final CryptUtility cryptUtility;
    private final ServerDataRepository serverDataRepository;
    @Value("${kb2.server.base-url}")
    private String serverBaseUrl;
    @Value("${kb2.server.session}")
    private String sessionEndpoint;
    @Value("${kb2.server.sign-up}")
    private String signUpEndpoint;
    @Value("${kb2.server.session.invalidate}")
    private String invalidateSessionEndpoint;
    private final RestTemplate template = new RestTemplate();

    @Transactional
    @SneakyThrows
    @Override
    public ServerData signUpForServer() {
        PublicKeyDto publicKeyDto = PublicKeyDto.builder()
                                                .base64Key(Base64.encodeBase64String(cryptUtility.getPublicKeyEncoded()))
                                                .build();
        ResponseEntity<ServerSignUpResponseDto> response = template.postForEntity(serverBaseUrl + signUpEndpoint, publicKeyDto, ServerSignUpResponseDto.class);
        ServerSignUpResponseDto responseDto = response.getBody();
        cryptUtility.saveServerPublicKey(Base64.decodeBase64(responseDto.getPublicKey()));
        ServerData serverData = new ServerData();
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getClientId()));
        serverData.setClientId(clientId);
        String session = Base64.encodeBase64String(cryptUtility.decodeBytesRSA(Base64.decodeBase64(responseDto.getSessionKey())));
        String iv = Base64.encodeBase64String(cryptUtility.decodeBytesRSA(Base64.decodeBase64(responseDto.getIv())));
        serverData.setSession(session);
        serverData.setStatus(CONNECTED.name());
        serverData.setIv(iv);
        return serverDataRepository.save(serverData);
    }

    @Transactional
    @SneakyThrows
    @Override
    public ServerData getNewSession() {
        ServerData serverData = getServerData();
        byte[] encodedClientId = cryptUtility.encodeStringForServerRSA(serverData.getClientId());
        SessionUpdateRequestDto requestDto = SessionUpdateRequestDto.builder()
                                                                    .encodedClientId(Base64.encodeBase64String(encodedClientId))
                                                                    .build();
        ResponseEntity<SessionKeyDto> response = template.postForEntity(serverBaseUrl + sessionEndpoint, requestDto, SessionKeyDto.class);
        SessionKeyDto sessionKeyDto = response.getBody();
        serverData.setSession(Base64.encodeBase64String(cryptUtility.decodeBytesRSA(Base64.decodeBase64(sessionKeyDto.getSessionKey()))));
        String iv = Base64.encodeBase64String(cryptUtility.decodeBytesRSA(Base64.decodeBase64(sessionKeyDto.getIv())));
        serverData.setStatus(CONNECTED.name());
        serverData.setIv(iv);
        return serverDataRepository.save(serverData);
    }

    @Transactional
    @SneakyThrows
    @Override
    public ServerData invalidateSession() {
        ServerData serverData = getServerData();
        byte[] encodedClientId = cryptUtility.encodeStringForServerRSA(serverData.getClientId());
        SessionUpdateRequestDto requestDto = SessionUpdateRequestDto.builder()
                                                                    .encodedClientId(Base64.encodeBase64String(encodedClientId))
                                                                    .build();
        template.postForEntity(serverBaseUrl + invalidateSessionEndpoint, requestDto, Void.class);
        serverData.setStatus(DISCONNECTED.name());
        return serverDataRepository.save(serverData);
    }

    @Override
    public String getConnectionStatus() {
        List<ServerData> serverDataList = serverDataRepository.findCurrentServerData(PageRequest.of(0, 1));
        return serverDataList.isEmpty() ? NO_SERVER.name() : serverDataList.get(0).getStatus();
    }

    private ServerData getServerData() {
        List<ServerData> serverDataList = serverDataRepository.findCurrentServerData(PageRequest.of(0, 1));
        if (serverDataList.isEmpty()) {
            throw new NoServerException("Not signed up for the server!");
        }
        return serverDataList.get(0);
    }

}
