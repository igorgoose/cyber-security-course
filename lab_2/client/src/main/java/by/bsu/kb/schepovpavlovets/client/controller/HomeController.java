package by.bsu.kb.schepovpavlovets.client.controller;

import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RequiredArgsConstructor
@Controller
@RequestMapping("")
public class HomeController {

    private final IntegrationService integrationService;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("connectionStatus", integrationService.getConnectionStatus());
        return "index";
    }

}
