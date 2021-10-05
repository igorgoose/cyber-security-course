package by.bsu.kb.schepovpavlovets.client.controller;

import by.bsu.kb.schepovpavlovets.client.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.client.service.FileService;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final IntegrationService integrationService;

    @GetMapping
    public String index(Model model) {
        List<FileShortDto> fileShortDtos = fileService.getFiles();
        model.addAttribute("files", fileShortDtos);
        model.addAttribute("connectionStatus", integrationService.getConnectionStatus());
        return "files/index";
    }

    @GetMapping("/{fileId}")
    public String one(@PathVariable String fileId, Model model) {
        FileDto fileDto = fileService.getFile(fileId);
        model.addAttribute("file", fileDto);
        model.addAttribute("connectionStatus", integrationService.getConnectionStatus());
        return "files/one";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("connectionStatus", integrationService.getConnectionStatus());
        return "files/create";
    }

    @PostMapping
    public String save(@ModelAttribute("filename") String filename,
            @ModelAttribute("content") String content) {
        fileService.saveFile(content, filename);
        return "redirect:/files";
    }
}
