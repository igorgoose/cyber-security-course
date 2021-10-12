package by.bsu.kb.schepovpavlovets.client.controller;

import by.bsu.kb.schepovpavlovets.client.model.dto.FileDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.FileShortDto;
import by.bsu.kb.schepovpavlovets.client.service.FileService;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.service.ServerConnectionService;
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
    private final ServerConnectionService serverConnectionService;

    @GetMapping
    public String index(Model model) {
        List<FileShortDto> fileShortDtos = fileService.getFiles();
        model.addAttribute("files", fileShortDtos);
        model.addAttribute("connectionStatus", serverConnectionService.getServerConnectionStatus());
        return "files/index";
    }

    @GetMapping("/{fileId}")
    public String one(@PathVariable String fileId, Model model) {
        FileDto fileDto = fileService.getFile(fileId);
        model.addAttribute("file", fileDto);
        model.addAttribute("connectionStatus", serverConnectionService.getServerConnectionStatus());
        return "files/one";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("connectionStatus", serverConnectionService.getServerConnectionStatus());
        return "files/create";
    }

    @GetMapping("/{fileId}/edit")
    public String edit(@PathVariable String fileId, Model model) {
        FileDto fileDto = fileService.getFile(fileId);
        model.addAttribute("file", fileDto);
        model.addAttribute("connectionStatus", serverConnectionService.getServerConnectionStatus());
        return "files/edit";
    }

    @PostMapping
    public String save(@ModelAttribute("filename") String filename,
            @ModelAttribute("content") String content) {
        fileService.saveFile(content, filename);
        return "redirect:/files";
    }

    @PostMapping("/{fileId}/edit")
    public String updateFile(@PathVariable String fileId, @ModelAttribute("filename") String filename,
            @ModelAttribute("content") String content) {
        fileService.editFile(fileId, filename, content);
        return "redirect:/files/" + fileId;
    }

    @PostMapping("/{fileId}/delete")
    public String deleteFile(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return "redirect:/files";
    }
}
