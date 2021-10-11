package by.bsu.kb.schepovpavlovets.server.controller;

import by.bsu.kb.schepovpavlovets.server.model.dto.*;
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
    public SignedMessageDto<List<FileShortDto>> getFiles(@CookieValue("client") String encodedCookie, @RequestParam("namespace") String encodedNamespace) {
        return fileService.getClientFiles(encodedCookie, encodedNamespace);
    }

    @GetMapping("/one")
    public SignedMessageDto<FileDto> getFile(@CookieValue("client") String encodedCookie, @RequestParam("namespace") String encodedNamespace, @RequestParam("fileId") String encodedFileId) {
        return fileService.getClientFile(encodedCookie, encodedNamespace, encodedFileId);
    }

    @PostMapping
    public void saveFile(@CookieValue("client") String encodedCookie, @RequestBody SignedMessageDto<FileRequestDto> signedRequest) {
        fileService.saveClientFile(encodedCookie, signedRequest);
    }

    @PutMapping
    public void editFile(@CookieValue("client") String encodedCookie, @RequestBody SignedMessageDto<EditFileRequestDto> signedRequest) {
        fileService.editClientFile(encodedCookie, signedRequest);
    }

    @PostMapping("/delete")
    public void deleteFile(@CookieValue("client") String encodedCookie, @RequestBody SignedMessageDto<DeleteFileRequestDto> signedRequest) {
        fileService.deleteClientFile(encodedCookie, signedRequest);
    }
}
