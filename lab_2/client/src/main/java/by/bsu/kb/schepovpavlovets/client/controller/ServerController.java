package by.bsu.kb.schepovpavlovets.client.controller;

import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerDto;
import by.bsu.kb.schepovpavlovets.client.service.ServerConnectionService;
import by.bsu.kb.schepovpavlovets.client.service.UserServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/servers")
public class ServerController {

    private final UserServerService userServerService;
    private final ServerConnectionService serverConnectionService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("servers", userServerService.getUserServers());
        return "servers/index";
    }

    @GetMapping("/create")
    public String getCreatePage(@ModelAttribute("server") UserServerDto userServerDto) {
        return "servers/create";
    }

    @PostMapping
    public String create(@ModelAttribute("server") UserServerDto userServerDto) {
        userServerService.saveUserServer(userServerDto.getName(), userServerDto.getIp(), userServerDto.getPort());
        return "redirect:/servers";
    }

    @PostMapping("/connect")
    public String connect(@ModelAttribute("userServerId") String userServerId) {
        serverConnectionService.connectToServer(UUID.fromString(userServerId));
        return "redirect:/files";
    }

    @PostMapping("/disconnect")
    public String disconnect(@ModelAttribute("userServerId") String userServerId) {
        serverConnectionService.disconnectFromServer(UUID.fromString(userServerId));
        return "redirect:/servers";
    }

}
