package by.bsu.kb.schepovpavlovets.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("")
public class HomeController {

    @GetMapping
    public String home() {
        return "redirect:/servers";
    }
}
