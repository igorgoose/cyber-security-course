package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.model.dto.*;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.repository.ServerDataRepository;
import by.bsu.kb.schepovpavlovets.client.security.AppUserDetails;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.util.CryptUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private static final String IP_URL_REGEX = "\\{server_ip}";
    private static final String PORT_URL_REGEX = "\\{server_port}";
    private final CryptUtility cryptUtility;
    private final ServerDataRepository serverDataRepository;
    @Value("${kb2.server.base-url}")
    private String serverBaseUrlTemplate;
    @Value("${kb2.server.connection}")
    private String connectionEndpoint;
    @Value("${kb2.server.sign-up}")
    private String signUpEndpoint;
    @Value("${kb2.server.disconnect}")
    private String disconnectEndpoint;
    @Value("${kb2.server.client.namespace}")
    private String serverNamespaceEndpoint;
    @Value("${kb2.server.cookie}")
    private String cookieName;
    private final RestTemplate template = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @SneakyThrows
    @Override
    public ServerData signUpForServer(String ip, String port) {
        PublicKeyDto publicKeyDto = PublicKeyDto.builder()
                                                .base64Key(Base64.encodeBase64String(cryptUtility.getPublicKeyEncoded()))
                                                .build();
        String serverBaseUrl = serverBaseUrlTemplate.replaceFirst(IP_URL_REGEX, ip).replaceFirst(PORT_URL_REGEX, port);
        ResponseEntity<ServerSignUpResponseDto> response = template.postForEntity(serverBaseUrl + signUpEndpoint, publicKeyDto, ServerSignUpResponseDto.class);
        ServerSignUpResponseDto responseDto = response.getBody();
        ServerData serverData = new ServerData();
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getClientId()));
        serverData.setClientId(clientId);
        serverData.setIp(ip);
        serverData.setPort(port);
        serverData.setUpdatedOn(LocalDateTime.now());
        serverDataRepository.save(serverData);
        cryptUtility.saveServerPublicKey(Base64.decodeBase64(responseDto.getPublicKey()), serverData.getId());
        return serverData;
    }

    @Transactional
    @SneakyThrows
    @Override
    public ServerConnectionResponseDto connect(ServerData serverData) {
        byte[] encodedClientId = cryptUtility.encodeStringForServerRSA(serverData.getClientId(), serverData.getId());
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        byte[] encodedNamespace = cryptUtility.encodeStringForServerRSA(userDetails.getId().toString(), serverData.getId());
        ConnectionRequestDto requestDto = ConnectionRequestDto.builder()
                                                              .encodedClientId(Base64.encodeBase64String(encodedClientId))
                                                              .encodedNamespace(Base64.encodeBase64String(encodedNamespace))
                                                              .build();
        String serverBaseUrl = serverBaseUrlTemplate.replaceFirst(IP_URL_REGEX, serverData.getIp()).replaceFirst(PORT_URL_REGEX, serverData.getPort());
        ResponseEntity<ServerConnectionResponseDto> response = template.postForEntity(serverBaseUrl + connectionEndpoint, requestDto, ServerConnectionResponseDto.class);
        ServerConnectionResponseDto responseDto = response.getBody();
        responseDto.setConnectionId(cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getConnectionId())));
        responseDto.setSessionKey(cryptUtility.decodeBytesToBase64RSA(Base64.decodeBase64(responseDto.getSessionKey())));
        responseDto.setIv(cryptUtility.decodeBytesToBase64RSA(Base64.decodeBase64(responseDto.getIv())));
        responseDto.setExpiresAt(cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(responseDto.getExpiresAt())));
        return response.getBody();
    }

    @SneakyThrows
    @Override
    public ServerErrorDto disconnect(ServerConnection serverConnection) {
        ServerData serverData = serverConnection.getUserServer().getServerData();
        byte[] encodedClientId = cryptUtility.encodeStringForServerRSA(serverData.getClientId(), serverData.getId());
        byte[] encodedConnectionId = cryptUtility.encodeStringForServerRSA(serverConnection.getId().toString(), serverData.getId());
        DisconnectRequestDto requestDto = DisconnectRequestDto.builder()
                .encodedClientId(Base64.encodeBase64String(encodedClientId))
                .encodedConnectionId(Base64.encodeBase64String(encodedConnectionId))
                .build();

        String serverBaseUrl = serverBaseUrlTemplate.replaceFirst(IP_URL_REGEX, serverData.getIp()).replaceFirst(PORT_URL_REGEX, serverData.getPort());
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverBaseUrl + disconnectEndpoint);
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestDto)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            return objectMapper.readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), ServerErrorDto.class);
        }
        return null;
    }

    @Override
    public String getConnectionStatus() {
        return "DISCONNECTED";
    }

}
