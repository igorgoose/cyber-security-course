package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileShortDto;
import lombok.SneakyThrows;

import javax.transaction.Transactional;
import java.util.List;

public interface FileService {
    void createClientFolder(String encodedClientId);

    @SneakyThrows
    List<FileShortDto> getClientFiles(String encodedClientId);

    @Transactional
    @SneakyThrows
    FileDto getClientFile(String encodedClientId, String encodedFileId);

    @Transactional
    @SneakyThrows
    void saveClientFile(String encodedClientId, String encodedFilename, String encodedContent);

    @Transactional
    @SneakyThrows
    void deleteClientFile(String encodedClientId, String encodedFileId);
}
