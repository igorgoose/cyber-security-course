package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.DeleteFileRequestDto;
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
    public List<FileShortDto> getFiles(@CookieValue("client") String encodedCookie, @RequestParam("namespace") String encodedNamespace) {
        return fileService.getClientFiles(encodedCookie, encodedNamespace);
    }

    @GetMapping("/one")
    public FileDto getFile(@CookieValue("client") String encodedCookie, @RequestParam("namespace") String encodedNamespace, @RequestParam("fileId") String encodedFileId) {
        return fileService.getClientFile(encodedCookie, encodedNamespace, encodedFileId);
    }

    @PostMapping
    public void saveFile(@CookieValue("client") String encodedCookie, @RequestBody FileRequestDto fileRequestDto) {
        fileService.saveClientFile(encodedCookie, fileRequestDto.getEncodedFilename(), fileRequestDto.getEncodedContent(), fileRequestDto.getEncodedNamespace());
    }

    @PostMapping("/delete")
    public void deleteFile(@CookieValue("client") String encodedCookie, @RequestBody DeleteFileRequestDto deleteFileRequestDto) {
        fileService.deleteClientFile(encodedCookie, deleteFileRequestDto.getEncodedNamespace(), deleteFileRequestDto.getEncodedFileId());
    }
}
