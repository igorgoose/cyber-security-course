package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.FileServiceException;
import by.bsu.kb.schepovpavlovets.client.exception.NoServerException;
import by.bsu.kb.schepovpavlovets.client.exception.ServerError;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileRequestDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerErrorDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.repository.ServerDataRepository;
import by.bsu.kb.schepovpavlovets.client.service.FileService;
import by.bsu.kb.schepovpavlovets.client.util.CryptUtility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final CryptUtility cryptUtility;
    private final ServerDataRepository serverDataRepository;
    @Value("${kb2.server.base-url}")
    private String serverBaseUrl;
    @Value("${kb2.server.files}")
    private String serverFilesEndpoint;
    @Value("${kb2.server.files.one}")
    private String serverFileEndpoint;
    @Value("${kb2.server.files.delete}")
    private String serverFilesDeleteEndpoint;
    @Value("${kb2.server.files.namespace}")
    private String serverFilesNamespaceEndpoint;
    @Value("${kb2.server.cookie}")
    private String cookieName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public void saveFile(String content, String filename) {
        ServerData serverData = getServerData();
        FileRequestDto fileRequestDto = FileRequestDto.builder()
                .encodedContent(cryptUtility.encryptSerpent(content, serverData.getSession(), serverData.getIv()))
                .encodedFilename(cryptUtility.encryptSerpent(filename, serverData.getSession(), serverData.getIv()))
                                                      .build();
        String encodedClientId = Base64.encodeBase64String(cryptUtility.encodeStringForServerRSA(serverData.getClientId()));

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverBaseUrl + serverFilesEndpoint);
        httpPost.setHeader("Cookie", cookieName + "=" + encodedClientId);
        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(fileRequestDto)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            processServerErrorResponse(serverData, response);
        }
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public List<FileShortDto> getFiles() {
        ServerData serverData = getServerData();
        String encodedClientId = Base64.encodeBase64String(cryptUtility.encodeStringForServerRSA(serverData.getClientId()));

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serverBaseUrl + serverFilesEndpoint);
        httpGet.setHeader("Cookie", cookieName + "=" + encodedClientId);
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            List<FileShortDto> fileShortDtos = objectMapper.readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), new TypeReference<>() {});
            fileShortDtos.forEach(fileShortDto -> decodeFileShortDto(fileShortDto, serverData));
            return fileShortDtos;
        } else {
            processServerErrorResponse(serverData, response);
        }
        throw new FileServiceException("Unexpected error occurred. Please try again later!");
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public FileDto getFile(String fileId) {
        ServerData serverData = getServerData();
        String encodedClientId = Base64.encodeBase64String(cryptUtility.encodeStringForServerRSA(serverData.getClientId()));
        String encodedFileId = cryptUtility.encryptSerpent(fileId, serverData.getSession(), serverData.getIv());
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(serverBaseUrl + serverFileEndpoint + "?fileId=" + URLEncoder.encode(encodedFileId, StandardCharsets.UTF_8));
        httpGet.setHeader("Cookie", cookieName + "=" + encodedClientId);
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            FileDto fileDto = objectMapper.readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), FileDto.class);
            decodeFileDto(fileDto, serverData);
            return fileDto;
        } else {
            processServerErrorResponse(serverData, response);
        }
        throw new FileServiceException("Unexpected error occurred. Please try again later!");
    }

    @SneakyThrows
    private void processServerErrorResponse(ServerData serverData, CloseableHttpResponse response) {
        ServerErrorDto serverErrorDto = objectMapper.readValue(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8), ServerErrorDto.class);
        if (ServerError.INVALID_SESSION.name().equals(serverErrorDto.getMessage()) || ServerError.SESSION_EXPIRED.name().equals(serverErrorDto.getMessage())){
            serverData.setStatus(ServerData.ConnectionStatus.DISCONNECTED.name());
            serverDataRepository.save(serverData);
        } else if (ServerError.INVALID_CLIENT_ID.name().equals(serverErrorDto.getMessage())) {
            serverDataRepository.deleteByClientId(serverData.getClientId());
        }
        throw new FileServiceException(serverErrorDto.getMessage());
    }

    @Transactional(dontRollbackOn = FileServiceException.class)
    @SneakyThrows
    @Override
    public void createNamespace(String namespace) {
        ServerData serverData = getServerData();
        String encodedClientId = Base64.encodeBase64String(cryptUtility.encodeStringForServerRSA(serverData.getClientId()));
        String encodedNamespace = cryptUtility.encryptSerpent(namespace, serverData.getSession(), serverData.getIv());
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(serverBaseUrl + serverFilesNamespaceEndpoint + "?namespace=" + encodedNamespace);
        httpPost.setHeader("Cookie", cookieName + "=" + encodedClientId);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = client.execute(httpPost);

        processServerErrorResponse(serverData, response);
    }

    private ServerData getServerData() {
        List<ServerData> serverDataList = serverDataRepository.findCurrentServerData(PageRequest.of(0, 1));
        if (serverDataList.isEmpty()) {
            throw new NoServerException("Not signed up for the server!");
        }
        return serverDataList.get(0);
    }

    private void decodeFileShortDto(FileShortDto fileShortDto, ServerData serverData) {
        fileShortDto.setFilename(cryptUtility.decryptSerpent(fileShortDto.getFilename(), serverData.getSession(), serverData.getIv()));
        fileShortDto.setId(cryptUtility.decryptSerpent(fileShortDto.getId(), serverData.getSession(), serverData.getIv()));
        fileShortDto.setLastUpdate(cryptUtility.decryptSerpent(fileShortDto.getLastUpdate(), serverData.getSession(), serverData.getIv()));
    }

    private void decodeFileDto(FileDto fileDto, ServerData serverData) {
        fileDto.setName(cryptUtility.decryptSerpent(fileDto.getName(), serverData.getSession(), serverData.getIv()));
        fileDto.setId(cryptUtility.decryptSerpent(fileDto.getId(), serverData.getSession(), serverData.getIv()));
        fileDto.setLastUpdate(cryptUtility.decryptSerpent(fileDto.getLastUpdate(), serverData.getSession(), serverData.getIv()));
        fileDto.setContent(cryptUtility.decryptSerpent(fileDto.getContent(), serverData.getSession(), serverData.getIv()));
    }
}
