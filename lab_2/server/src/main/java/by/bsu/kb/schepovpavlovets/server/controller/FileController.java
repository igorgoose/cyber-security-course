package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileRequestDto;
import by.bsu.kb.schepovpavlovets.server.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.server.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public List<FileShortDto> getFiles(@CookieValue("clientId") String encodedClientId) {
        return fileService.getClientFiles(encodedClientId);
    }

    @GetMapping("/one")
    public FileDto getFile(@CookieValue("clientId") String encodedClientId, @RequestParam("fileId") String encodedFileId) {
        return fileService.getClientFile(encodedClientId, encodedFileId);
    }

    @PostMapping
    public void saveFile(@CookieValue("clientId") String encodedClientId, @RequestBody FileRequestDto fileRequestDto) {
        fileService.saveClientFile(encodedClientId, fileRequestDto.getEncodedFilename(), fileRequestDto.getEncodedContent(), fileRequestDto.getEncodedNamespace());
    }

    @PostMapping("/delete")
    public void deleteFile(@CookieValue("clientId") String encodedClientId, @RequestParam("fileId") String encodedFileId) {
        fileService.deleteClientFile(encodedClientId, encodedFileId);
    }

    @PostMapping("/namespace")
    public void createFolders(@CookieValue("clientId") String encodedClientId, @RequestParam("namespace") String encodedNamespace) {
        fileService.createNamespace(encodedClientId, encodedNamespace);
    }
}
