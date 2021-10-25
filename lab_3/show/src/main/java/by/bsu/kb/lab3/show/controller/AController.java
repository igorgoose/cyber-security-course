package by.bsu.kb.lab3.show.controller;

import by.bsu.kb.lab3.show.dto.*;
import by.bsu.kb.schepov.lab3.utils.EncryptionConfig;
import by.bsu.kb.schepov.lab3.utils.EncryptionUtil;
import by.bsu.kb.schepov.lab3.utils.Signature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/A")
@RestController
public class AController {

    @Value("${security.modulus}")
    private int modulus;
    private EncryptionConfig encryptionConfig = null;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/config")
    public EncryptionConfig getEncryptionConfig() {
        return encryptionConfig;
    }

    @PostMapping("/config")
    public EncryptionConfig generateEncryptionConfig() {
        encryptionConfig = new EncryptionConfig(modulus);
        return encryptionConfig;
    }

    @PostMapping("/exchange")
    public EncryptionConfig exchangeKeys() {
        ExchangeResponseDto exchangeResponseDto = restTemplate.postForEntity("http://localhost:8080/B/exchange",
                new ExchangeRequestDto(encryptionConfig.getBasePoint(), encryptionConfig.getPublicKey()), ExchangeResponseDto.class).getBody();
        encryptionConfig.computeCommonPrivateKey(exchangeResponseDto.getPublicKey());
        return encryptionConfig;
    }

    @PostMapping("/send")
    public SendReportDto sendBroken(@RequestParam String message, @RequestParam(required = false) String brokenMessage) {
        Signature signature = EncryptionUtil.sign(message, encryptionConfig.getBasePoint(), encryptionConfig.getPrivateKey());
        SendResponseDto sendResponseDto = restTemplate.postForEntity("http://localhost:8080/B/receive",
                new SendRequestDto(brokenMessage == null ? message : brokenMessage, signature),
                SendResponseDto.class).getBody();
        return new SendReportDto(message, sendResponseDto);
    }
}
