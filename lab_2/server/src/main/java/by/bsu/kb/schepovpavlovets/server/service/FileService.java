package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileShortDto;
import lombok.SneakyThrows;

import javax.transaction.Transactional;
import java.util.List;

public interface FileService {
    void createClientFolder(String encodedClientId);

    @SneakyThrows
    List<FileShortDto> getClientFiles(String encodedClientId, String encodedNamespace);

    @Transactional
    @SneakyThrows
    FileDto getClientFile(String encodedCookie, String encodedNamespace, String encodedFileId);

    @Transactional
    @SneakyThrows
    void saveClientFile(String encodedCookie, String encodedFilename, String encodedContent, String encodedNamespace);

    @Transactional
    @SneakyThrows
    void deleteClientFile(String encodedClientId, String encodedNamespace, String encodedFileId);

    void createNamespace(String clientId, String namespace);
}
