package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class SignedMessageDto<T> {
    private T content;
    private String signature;
    private String algorithm;
}
