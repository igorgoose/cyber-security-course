package by.bsu.kb.schepovpavlovets.server.service.impl;

import by.bsu.kb.schepovpavlovets.server.exception.NotFoundException;
import by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.server.model.entity.ClientConnection;
import by.bsu.kb.schepovpavlovets.server.model.entity.FileEntity;
import by.bsu.kb.schepovpavlovets.server.repository.ClientConnectionRepository;
import by.bsu.kb.schepovpavlovets.server.repository.ClientRepository;
import by.bsu.kb.schepovpavlovets.server.repository.FileRepository;
import by.bsu.kb.schepovpavlovets.server.service.FileService;
import by.bsu.kb.schepovpavlovets.server.util.CryptUtility;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException.Status.*;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private static final String CLIENT_ID_REGEX = "\\{clientId}";
    private static final String NAMESPACE_REGEX = "\\{namespace}";
    private static final String FILE_ID_REGEX = "\\{fileId}";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss");
    private final ClientRepository clientRepository;
    private final ClientConnectionRepository clientConnectionRepository;
    private final FileRepository fileRepository;
    private final CryptUtility cryptUtility;
    @Value("${content.env-var}")
    private String contentEnvVar;
    @Value("${content.files.client.folder}")
    private String clientFilesFolder;
    @Value("${content.files.folder}")
    private String filesFolder;
    @Value("${content.files.name}")
    private String clientFilePath;

    @SneakyThrows
    @PostConstruct
    private void init() {
        File filesFolder = new File(System.getenv(contentEnvVar) + this.filesFolder);
        if (!filesFolder.exists()) {
            Files.createDirectory(filesFolder.toPath());
        }
    }

    @SneakyThrows
    @Override
    public void createClientFolder(String clientId) {
        String clientFolderPath = (System.getenv(contentEnvVar) + clientFilesFolder).replaceFirst(CLIENT_ID_REGEX, clientId);
        Files.createDirectory(new File(clientFolderPath).toPath());
    }

    @Transactional
    @SneakyThrows
    @Override
    public List<FileShortDto> getClientFiles(String encodedCookie, String encodedNamespace) {
        ClientConnection clientConnection = processClientCookie(encodedCookie);
        String namespace = cryptUtility.decryptSerpent(encodedNamespace, clientConnection.getSession(), clientConnection.getIv());
        List<FileEntity> fileEntities = fileRepository.findByClientIdAndNamespaceOrderByUpdatedOnDesc(clientConnection.getClient().getId(), namespace);
        return fileEntities.stream().map(fileEntity_ -> convertToShortDto(fileEntity_, clientConnection)).collect(Collectors.toList());
    }

    @Transactional
    @SneakyThrows
    @Override
    public FileDto getClientFile(String encodedCookie, String encodedNamespace, String encodedFileId) {
        ClientConnection clientConnection = processClientCookie(encodedCookie);
        String fileId = cryptUtility.decryptSerpent(encodedFileId, clientConnection.getSession(), clientConnection.getIv());
        String namespace = cryptUtility.decryptSerpent(encodedNamespace, clientConnection.getSession(), clientConnection.getIv());
        FileEntity fileEntity = fileRepository.findByIdAndNamespace(UUID.fromString(fileId), namespace).orElseThrow(() -> new NotFoundException("File not found"));
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath)
                .replaceFirst(CLIENT_ID_REGEX, clientConnection.getClient().getId().toString())
                .replaceFirst(NAMESPACE_REGEX, namespace)
                .replaceFirst(FILE_ID_REGEX, fileId));
        String content = Files.readString(file.toPath());
        String encodedContent = cryptUtility.encryptSerpent(content, clientConnection.getSession(), clientConnection.getIv());
        String encodedFileName = cryptUtility.encryptSerpent(fileEntity.getName(), clientConnection.getSession(), clientConnection.getIv());
        String encodedLastUpdate = cryptUtility.encryptSerpent(dtf.format(fileEntity.getUpdatedOn()), clientConnection.getSession(), clientConnection.getIv());
        return FileDto.builder()
                      .id(encodedFileId)
                      .content(encodedContent)
                      .name(encodedFileName)
                      .lastUpdate(encodedLastUpdate)
                      .build();
    }

    @Transactional
    @SneakyThrows
    @Override
    public void saveClientFile(String encodedCookie, String encodedFilename, String encodedContent, String encodedNamespace) {
        ClientConnection clientConnection = processClientCookie(encodedCookie);
        String content = cryptUtility.decryptSerpent(encodedContent, clientConnection.getSession(), clientConnection.getIv());
        String filename = cryptUtility.decryptSerpent(encodedFilename, clientConnection.getSession(), clientConnection.getIv());
        String namespace = cryptUtility.decryptSerpent(encodedNamespace, clientConnection.getSession(), clientConnection.getIv());
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(filename);
        fileEntity.setClientId(clientConnection.getClient().getId());
        fileEntity.setUpdatedOn(LocalDateTime.now());
        fileEntity.setNamespace(namespace);
        fileRepository.save(fileEntity);
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath)
                .replaceFirst(CLIENT_ID_REGEX, clientConnection.getClient().getId().toString())
                .replaceFirst(NAMESPACE_REGEX, namespace)
                .replaceFirst(FILE_ID_REGEX, fileEntity.getId().toString())
        );
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Transactional
    @SneakyThrows
    @Override
    public void deleteClientFile(String encodedCookie, String encodedNamespace, String encodedFileId) {
        ClientConnection clientConnection = processClientCookie(encodedCookie);
        String fileId = cryptUtility.decryptSerpent(encodedFileId, clientConnection.getSession(), clientConnection.getIv());
        String namespace = cryptUtility.decryptSerpent(encodedNamespace, clientConnection.getSession(), clientConnection.getIv());
        FileEntity fileEntity = fileRepository.findByIdAndNamespace(UUID.fromString(fileId), namespace).orElseThrow(() -> new NotFoundException("File not found"));
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath)
                .replaceFirst(CLIENT_ID_REGEX, clientConnection.getClient().getId().toString())
                .replaceFirst(NAMESPACE_REGEX, namespace)
                .replaceFirst(FILE_ID_REGEX, fileEntity.getId().toString()));
        Files.delete(file.toPath());
        fileRepository.deleteById(fileEntity.getId());
    }

    @SneakyThrows
    @Override
    public void createNamespace(String clientId, String namespace) {
        if (namespace.equals("")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Namespace param must not be empty!");
        }
        File newFolders = new File(System.getenv(contentEnvVar) + clientFilesFolder.replaceFirst(CLIENT_ID_REGEX, clientId + "/" + namespace));
        if (!newFolders.exists()) {
            Files.createDirectory(newFolders.toPath());
        }
    }

    private FileShortDto convertToShortDto(FileEntity fileEntity, ClientConnection clientConnection) {
        return FileShortDto.builder()
                           .id(cryptUtility.encryptSerpent(fileEntity.getId().toString(), clientConnection.getSession(), clientConnection.getIv()))
                           .filename(cryptUtility.encryptSerpent(fileEntity.getName(), clientConnection.getSession(), clientConnection.getIv()))
                           .lastUpdate(cryptUtility.encryptSerpent(dtf.format(fileEntity.getUpdatedOn()), clientConnection.getSession(), clientConnection.getIv()))
                           .build();
    }

    private ClientConnection processClientCookie(String encodedClientCookie) {
        String clientCookie = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(encodedClientCookie));
        String[] cookieData = clientCookie.split(":");
        if (cookieData.length != 2) {
            throw new UnauthorizedException(INVALID_COOKIE.name());
        }
        if (!clientRepository.existsById(UUID.fromString(cookieData[0]))) {
            throw new UnauthorizedException(INVALID_CLIENT_ID.name());
        }
        ClientConnection clientConnection = clientConnectionRepository.findById(UUID.fromString(cookieData[1]))
                                                                      .orElseThrow(() -> new UnauthorizedException(UNKNOWN_CONNECTION.name()));
        if (clientConnection.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException(SESSION_EXPIRED.name());
        }
        return clientConnection;
    }

}
