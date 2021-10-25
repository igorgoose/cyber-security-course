package by.bsu.kb.lab3.show.controller;

import by.bsu.kb.lab3.show.dto.ExchangeRequestDto;
import by.bsu.kb.lab3.show.dto.ExchangeResponseDto;
import by.bsu.kb.lab3.show.dto.SendRequestDto;
import by.bsu.kb.lab3.show.dto.SendResponseDto;
import by.bsu.kb.schepov.lab3.utils.EncryptionConfig;
import by.bsu.kb.schepov.lab3.utils.EncryptionUtil;
import by.bsu.kb.schepov.lab3.utils.Point;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/B")
@RestController
public class BController {
    private EncryptionConfig encryptionConfig = null;
    private Point publicKeyA;

    @GetMapping("/config")
    public EncryptionConfig getEncryptionConfig() {
        return encryptionConfig;
    }

    @PostMapping("/exchange")
    public ExchangeResponseDto exchangeKeys(@RequestBody ExchangeRequestDto exchangeRequestDto) {
        encryptionConfig = new EncryptionConfig(exchangeRequestDto.getBasePoint());
        encryptionConfig.computeCommonPrivateKey(exchangeRequestDto.getPublicKey());
        publicKeyA = exchangeRequestDto.getPublicKey();
        return new ExchangeResponseDto(encryptionConfig.getPublicKey());
    }

    @PostMapping("/receive")
    public SendResponseDto receive(@RequestBody SendRequestDto sendRequestDto) {
        boolean result = EncryptionUtil.verify(sendRequestDto.getMessage(), sendRequestDto.getSignature(), encryptionConfig.getBasePoint(), publicKeyA);
        return new SendResponseDto(sendRequestDto.getMessage(), sendRequestDto.getSignature(), result ? "Verified successfully!" : "Verification failed!");
    }

}
