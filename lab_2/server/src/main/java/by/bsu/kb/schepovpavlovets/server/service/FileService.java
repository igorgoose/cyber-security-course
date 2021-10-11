package by.bsu.kb.schepovpavlovets.server.service;

import by.bsu.kb.schepovpavlovets.server.model.dto.*;
import lombok.SneakyThrows;

import javax.transaction.Transactional;
import java.util.List;

public interface FileService {
    void createClientFolder(String encodedClientId);

    @SneakyThrows
    SignedMessageDto<List<FileShortDto>> getClientFiles(String encodedClientId, String encodedNamespace);

    @Transactional
    @SneakyThrows
    SignedMessageDto<FileDto> getClientFile(String encodedCookie, String encodedNamespace, String encodedFileId);

    @Transactional
    @SneakyThrows
    void saveClientFile(String encodedCookie, SignedMessageDto<FileRequestDto> signedRequest);

    @Transactional
    @SneakyThrows
    void deleteClientFile(String encodedClientId, SignedMessageDto<DeleteFileRequestDto> signedRequest);

    void createNamespace(String clientId, String namespace);

    void editClientFile(String encodedCookie, SignedMessageDto<EditFileRequestDto> signedRequest);
}
