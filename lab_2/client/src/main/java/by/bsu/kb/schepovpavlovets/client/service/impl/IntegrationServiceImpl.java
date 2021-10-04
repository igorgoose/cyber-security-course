package by.bsu.kb.schepovpavlovets.client.service.impl;

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
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private final CryptUtility cryptUtility;
    private final ServerDataRepository serverDataRepository;
    @Value("${kb2.server.base-url")
    private String serverBaseUrl;
    @Value("${kb2.server.session")
    private String sessionEndpoint;
    @Value("${kb2.server.sign-up")
    private String signUpEndpoint;
    private final RestTemplate template = new RestTemplate();

    @SneakyThrows
    @Override
    public void signUpForServer() {
        PublicKeyDto publicKeyDto = PublicKeyDto.builder()
                                                .base64Key(Base64.encodeBase64String(cryptUtility.getPublicKeyEncoded()))
                                                .build();
        ResponseEntity<ServerSignUpResponseDto> response = template.postForEntity(serverBaseUrl + signUpEndpoint, publicKeyDto, ServerSignUpResponseDto.class);
        ServerSignUpResponseDto responseDto = response.getBody();
        cryptUtility.saveServerPublicKey(Base64.decodeBase64(responseDto.getPublicKey()));
        ServerData serverData = new ServerData();
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getClientId()));
        serverData.setClientId(clientId);
        String session = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getSessionKey()));
        serverData.setSession(session);
        serverDataRepository.save(serverData);
    }

    @SneakyThrows
    @Override
    public void getNewSession() {
        ServerData serverData = serverDataRepository.findCurrentServerData(PageRequest.of(0, 1)).orElseThrow();
        byte[] encodedClientId = cryptUtility.encodeStringForServerRSA(serverData.getClientId());
        SessionUpdateRequestDto requestDto = SessionUpdateRequestDto.builder()
                                                                    .encodedClientId(Base64.encodeBase64String(encodedClientId))
                                                                    .build();
        ResponseEntity<SessionKeyDto> response = template.postForEntity(serverBaseUrl + sessionEndpoint, requestDto, SessionKeyDto.class);
        SessionKeyDto sessionKeyDto = response.getBody();
        serverData.setSession(cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(sessionKeyDto.getSessionKey())));
        serverDataRepository.save(serverData);
    }

}
