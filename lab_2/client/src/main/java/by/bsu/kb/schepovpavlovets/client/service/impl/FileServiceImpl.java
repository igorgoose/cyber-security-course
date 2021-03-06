package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.FileServiceException;
import by.bsu.kb.schepovpavlovets.client.exception.ServerError;
import by.bsu.kb.schepovpavlovets.client.model.dto.*;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.security.AppUserDetails;
import by.bsu.kb.schepovpavlovets.client.service.FileService;
import by.bsu.kb.schepovpavlovets.client.service.ServerConnectionService;
import by.bsu.kb.schepovpavlovets.client.util.CryptUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private static final String IP_URL_REGEX = "\\{server_ip}";
    private static final String PORT_URL_REGEX = "\\{server_port}";
    private final CryptUtility cryptUtility;
    private final ServerConnectionService serverConnectionService;
    @Value("${kb2.server.base-url}")
    private String serverBaseUrlTemplate;
    @Value("${kb2.server.files}")
    private String serverFilesEndpoint;
    @Value("${kb2.server.files.one}")
    private String serverFileEndpoint;
    @Value("${kb2.server.files.delete}")
    private String serverFilesDeleteEndpoint;
    @Value("${kb2.server.cookie}")
    private String cookieName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public void saveFile(String content, String filename) {
        ServerConnection serverConnection = serverConnectionService.getCurrentServerConnection();
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String encodedNamespace = cryptUtility.encryptSerpent(userDetails.getId().toString(), serverConnection.getSession(), serverConnection.getIv());

        FileRequestDto fileRequestDto = FileRequestDto.builder()
                                                      .encodedContent(
                                                              cryptUtility.encryptSerpent(content, serverConnection.getSession(), serverConnection.getIv()))
                                                      .encodedFilename(
                                                              cryptUtility.encryptSerpent(filename, serverConnection.getSession(), serverConnection.getIv()))
                                                      .encodedNamespace(encodedNamespace)
                                                      .build();
        SignedMessageDto<FileRequestDto> signedRequest = cryptUtility.signRequest(fileRequestDto);

        CloseableHttpClient client = HttpClients.createDefault();
        String serverBaseUrl = serverBaseUrlTemplate
                .replaceFirst(IP_URL_REGEX, serverConnection.getUserServer().getServerData().getIp())
                .replaceFirst(PORT_URL_REGEX, serverConnection.getUserServer().getServerData().getPort());
        HttpPost httpPost = new HttpPost(serverBaseUrl + serverFilesEndpoint);
        httpPost.setHeader("Cookie", cookieName + "=" + encodeCookie(serverConnection));
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(signedRequest)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            processServerErrorResponse(serverConnection, response);
        }
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public List<FileShortDto> getFiles() {
        ServerConnection serverConnection = serverConnectionService.getCurrentServerConnection();
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String encodedNamespace = cryptUtility.encryptSerpent(userDetails.getId().toString(), serverConnection.getSession(), serverConnection.getIv());
        String serverBaseUrl = serverBaseUrlTemplate
                .replaceFirst(IP_URL_REGEX, serverConnection.getUserServer().getServerData().getIp())
                .replaceFirst(PORT_URL_REGEX, serverConnection.getUserServer().getServerData().getPort());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serverBaseUrl + serverFilesEndpoint + "?namespace=" + URLEncoder.encode(encodedNamespace, StandardCharsets.UTF_8));
        httpGet.setHeader("Cookie", cookieName + "=" + encodeCookie(serverConnection));
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            SignedMessageDto<List<FileShortDto>> signedResponse = objectMapper
                    .readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), new TypeReference<>() {
                    });
            cryptUtility.verifySignature(signedResponse, serverConnection.getUserServer().getServerData().getId());
            List<FileShortDto> fileShortDtos = signedResponse.getContent();
            fileShortDtos.forEach(fileShortDto -> decodeFileShortDto(fileShortDto, serverConnection));
            return fileShortDtos;
        } else {
            processServerErrorResponse(serverConnection, response);
        }
        throw new FileServiceException("Unexpected error occurred. Please try again later!");
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public FileDto getFile(String fileId) {
        ServerConnection serverConnection = serverConnectionService.getCurrentServerConnection();
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String encodedNamespace = cryptUtility.encryptSerpent(userDetails.getId().toString(), serverConnection.getSession(), serverConnection.getIv());
        String encodedFileId = cryptUtility.encryptSerpent(fileId, serverConnection.getSession(), serverConnection.getIv());
        String serverBaseUrl = serverBaseUrlTemplate
                .replaceFirst(IP_URL_REGEX, serverConnection.getUserServer().getServerData().getIp())
                .replaceFirst(PORT_URL_REGEX, serverConnection.getUserServer().getServerData().getPort());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serverBaseUrl + serverFileEndpoint + "?fileId=" + URLEncoder.encode(encodedFileId, StandardCharsets.UTF_8)
                + "&namespace=" + URLEncoder.encode(encodedNamespace, StandardCharsets.UTF_8));
        httpGet.setHeader("Cookie", cookieName + "=" + encodeCookie(serverConnection));
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            SignedMessageDto<FileDto> signedResponse = objectMapper
                    .readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), new TypeReference<>() {
                    });
            cryptUtility.verifySignature(signedResponse, serverConnection.getUserServer().getServerData().getId());
            FileDto fileDto = signedResponse.getContent();
            decodeFileDto(fileDto, serverConnection);
            return fileDto;
        } else {
            processServerErrorResponse(serverConnection, response);
        }
        throw new FileServiceException("Unexpected error occurred. Please try again later!");
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public void editFile(String fileId, String filename, String content) {
        ServerConnection serverConnection = serverConnectionService.getCurrentServerConnection();
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String encodedNamespace = cryptUtility.encryptSerpent(userDetails.getId().toString(), serverConnection.getSession(), serverConnection.getIv());
        String encodedFileId = cryptUtility.encryptSerpent(fileId, serverConnection.getSession(), serverConnection.getIv());
        String encodedFilename = cryptUtility.encryptSerpent(filename, serverConnection.getSession(), serverConnection.getIv());
        String encodedContent = cryptUtility.encryptSerpent(content, serverConnection.getSession(), serverConnection.getIv());
        String serverBaseUrl = serverBaseUrlTemplate
                .replaceFirst(IP_URL_REGEX, serverConnection.getUserServer().getServerData().getIp())
                .replaceFirst(PORT_URL_REGEX, serverConnection.getUserServer().getServerData().getPort());

        EditFileRequestDto editFileRequestDto = EditFileRequestDto.builder()
                                                                  .fileId(encodedFileId)
                                                                  .namespace(encodedNamespace)
                                                                  .name(encodedFilename)
                                                                  .content(encodedContent)
                                                                  .build();

        SignedMessageDto<EditFileRequestDto> signedRequest = cryptUtility.signRequest(editFileRequestDto);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(serverBaseUrl + serverFilesEndpoint);
        httpPut.setHeader("Cookie", cookieName + "=" + encodeCookie(serverConnection));
        httpPut.setEntity(new StringEntity(objectMapper.writeValueAsString(signedRequest)));
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPut);

        if (response.getStatusLine().getStatusCode() != 200) {
            processServerErrorResponse(serverConnection, response);
        }
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public void deleteFile(String fileId) {
        ServerConnection serverConnection = serverConnectionService.getCurrentServerConnection();
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String encodedNamespace = cryptUtility.encryptSerpent(userDetails.getId().toString(), serverConnection.getSession(), serverConnection.getIv());
        String encodedFileId = cryptUtility.encryptSerpent(fileId, serverConnection.getSession(), serverConnection.getIv());

        DeleteFileRequestDto fileRequestDto = DeleteFileRequestDto.builder()
                                                                  .encodedFileId(encodedFileId)
                                                                  .encodedNamespace(encodedNamespace)
                                                                  .build();

        SignedMessageDto<DeleteFileRequestDto> signedRequest = cryptUtility.signRequest(fileRequestDto);

        CloseableHttpClient client = HttpClients.createDefault();
        String serverBaseUrl = serverBaseUrlTemplate
                .replaceFirst(IP_URL_REGEX, serverConnection.getUserServer().getServerData().getIp())
                .replaceFirst(PORT_URL_REGEX, serverConnection.getUserServer().getServerData().getPort());

        HttpPost httpPost = new HttpPost(serverBaseUrl + serverFilesDeleteEndpoint);
        httpPost.setHeader("Cookie", cookieName + "=" + encodeCookie(serverConnection));
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(signedRequest)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            processServerErrorResponse(serverConnection, response);
        }
    }

    @SneakyThrows
    private void processServerErrorResponse(ServerConnection serverConnection, CloseableHttpResponse response) {
        ServerErrorDto signedResponse = objectMapper
                .readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), new TypeReference<>() {
                });
        if (ServerError.SESSION_EXPIRED.name().equals(signedResponse.getMessage())) {
            serverConnectionService.disconnectFromServer(serverConnection.getUserServer().getId());
            throw new FileServiceException("Session expired! You've been disconnected!");
        }
        throw new FileServiceException(signedResponse.getMessage());
    }

    private void decodeFileShortDto(FileShortDto fileShortDto, ServerConnection serverConnection) {
        fileShortDto.setFilename(cryptUtility.decryptSerpent(fileShortDto.getFilename(), serverConnection.getSession(), serverConnection.getIv()));
        fileShortDto.setId(cryptUtility.decryptSerpent(fileShortDto.getId(), serverConnection.getSession(), serverConnection.getIv()));
        fileShortDto.setLastUpdate(cryptUtility.decryptSerpent(fileShortDto.getLastUpdate(), serverConnection.getSession(), serverConnection.getIv()));
    }

    private void decodeFileDto(FileDto fileDto, ServerConnection serverConnection) {
        fileDto.setName(cryptUtility.decryptSerpent(fileDto.getName(), serverConnection.getSession(), serverConnection.getIv()));
        fileDto.setId(cryptUtility.decryptSerpent(fileDto.getId(), serverConnection.getSession(), serverConnection.getIv()));
        fileDto.setLastUpdate(cryptUtility.decryptSerpent(fileDto.getLastUpdate(), serverConnection.getSession(), serverConnection.getIv()));
        fileDto.setContent(cryptUtility.decryptSerpent(fileDto.getContent(), serverConnection.getSession(), serverConnection.getIv()));
    }

    private String encodeCookie(ServerConnection serverConnection) {
        ServerData serverData = serverConnection.getUserServer().getServerData();
        return serverData.getClientId() + ":" + serverConnection.getId().toString();
    }
}
