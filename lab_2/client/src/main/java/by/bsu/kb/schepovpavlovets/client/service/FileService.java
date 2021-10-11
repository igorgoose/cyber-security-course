package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileShortDto;
import lombok.SneakyThrows;

import java.util.List;

public interface FileService {
    void saveFile(String content, String filename);

    @SneakyThrows
    List<FileShortDto> getFiles();

    @SneakyThrows
    FileDto getFile(String fileId);

    void editFile(String fileId, String filename, String content);

    void deleteFile(String fileId);
}
