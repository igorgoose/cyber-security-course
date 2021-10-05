package by.bsu.kb.schepovpavlovets.client.controller;

import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static by.bsu.kb.schepovpavlovets.client.model.entity.ServerData.ConnectionStatus.NO_SERVER;

@RequiredArgsConstructor
@Controller
@RequestMapping("/integration")
public class IntegrationController {
    private final IntegrationService integrationService;

    @PostMapping("/connect")
    public String connect(Model model) {
        String connectionStatus = integrationService.getConnectionStatus();
        ServerData serverData;
        if (NO_SERVER.name().equals(connectionStatus)) {
            serverData = integrationService.signUpForServer();
        } else {
            serverData = integrationService.getNewSession();
        }
        model.addAttribute("connectionStatus", serverData.getStatus());
        return "redirect:/";
    }

    @PostMapping("/disconnect")
    public String disconnect(Model model) {
        ServerData serverData = integrationService.invalidateSession();
        model.addAttribute("connectionStatus", serverData.getStatus());
        return "redirect:/";
    }
}
