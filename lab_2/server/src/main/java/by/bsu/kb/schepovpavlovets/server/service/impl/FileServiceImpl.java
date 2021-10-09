package by.bsu.kb.schepovpavlovets.server.service.impl;

import by.bsu.kb.schepovpavlovets.server.exception.NotFoundException;
import by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.server.model.entity.Client;
import by.bsu.kb.schepovpavlovets.server.model.entity.FileEntity;
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
    private static final String FILE_ID_REGEX = "\\{fileId}";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss");
    private final ClientRepository clientRepository;
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
    public List<FileShortDto> getClientFiles(String encodedClientId) {
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(encodedClientId));
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        if (client.getDisabled()) {
            throw new UnauthorizedException(INVALID_SESSION.name());
        }
        List<FileEntity> fileEntities = fileRepository.findByClientIdOrderByUpdatedOnDesc(client.getId());
        return fileEntities.stream().map(fileEntity_ -> convertToShortDto(fileEntity_, client)).collect(Collectors.toList());
    }

    @Transactional
    @SneakyThrows
    @Override
    public FileDto getClientFile(String encodedClientId, String encodedFileId) {
        Client client = getCurrentClient(encodedClientId);
        String fileId = cryptUtility.decryptSerpent(encodedFileId, client.getSession(), client.getIv());
        FileEntity fileEntity = fileRepository.findById(UUID.fromString(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath).replaceFirst(CLIENT_ID_REGEX, client.getId().toString()).replaceFirst(FILE_ID_REGEX, fileId));
        String content = Files.readString(file.toPath());
        String encodedContent = cryptUtility.encryptSerpent(content, client.getSession(), client.getIv());
        String encodedFileName = cryptUtility.encryptSerpent(fileEntity.getName(), client.getSession(), client.getIv());
        String encodedLastUpdate = cryptUtility.encryptSerpent(dtf.format(fileEntity.getUpdatedOn()), client.getSession(), client.getIv());
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
    public void saveClientFile(String encodedClientId, String encodedFilename, String encodedContent, String encodedFolder) {
        Client client = getCurrentClient(encodedClientId);
        String content = cryptUtility.decryptSerpent(encodedContent, client.getSession(), client.getIv());
        String filename = cryptUtility.decryptSerpent(encodedFilename, client.getSession(), client.getIv());
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(filename);
        fileEntity.setClientId(client.getId());
        fileEntity.setUpdatedOn(LocalDateTime.now());
        fileRepository.save(fileEntity);
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath).replaceFirst(CLIENT_ID_REGEX, client.getId().toString()).replaceFirst(FILE_ID_REGEX, fileEntity.getId().toString()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Transactional
    @SneakyThrows
    @Override
    public void deleteClientFile(String encodedClientId, String encodedFileId) {
        Client client = getCurrentClient(encodedClientId);
        String fileId = cryptUtility.decryptSerpent(encodedFileId, client.getSession(), client.getIv());
        FileEntity fileEntity = fileRepository.findById(UUID.fromString(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientFilePath).replaceFirst(CLIENT_ID_REGEX, client.getId().toString()).replaceFirst(FILE_ID_REGEX, fileEntity.getId().toString()));
        Files.delete(file.toPath());
        fileRepository.deleteById(fileEntity.getId());
    }

    @SneakyThrows
    @Override
    public void createNamespace(String encodedClientId, String encodedNamespace) {
        Client client = getCurrentClient(encodedClientId);
        if (encodedNamespace.equals("")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Namespace param must not be empty!");
        }
        String folders = cryptUtility.decryptSerpent(encodedNamespace, client.getSession(), client.getIv());
        if (folders.equals("")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Namespace param must not be empty!");
        }
        File newFolders = new File(System.getenv(contentEnvVar) + clientFilesFolder.replaceFirst(CLIENT_ID_REGEX, client.getId().toString()) + folders);
        Files.createDirectory(newFolders.toPath());
    }

    private FileShortDto convertToShortDto(FileEntity fileEntity, Client client) {
        return FileShortDto.builder()
                           .id(cryptUtility.encryptSerpent(fileEntity.getId().toString(), client.getSession(), client.getIv()))
                           .filename(cryptUtility.encryptSerpent(fileEntity.getName(), client.getSession(), client.getIv()))
                           .lastUpdate(cryptUtility.encryptSerpent(dtf.format(fileEntity.getUpdatedOn()), client.getSession(), client.getIv()))
                           .build();
    }

    private Client getCurrentClient(String encodedClientId) {
        String clientId = cryptUtility.decodeBytesToStringRSA(Base64.decodeBase64(encodedClientId));
        Client client = clientRepository.findById(UUID.fromString(clientId)).orElseThrow(() -> new UnauthorizedException(INVALID_CLIENT_ID.name()));
        if (client.getDisabled()) {
            throw new UnauthorizedException(INVALID_SESSION.name());
        }
        if (client.getSessionExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException(SESSION_EXPIRED.name());
        }
        return client;
    }

}
