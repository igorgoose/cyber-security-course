package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Data;

@Data
public class SignedMessageDto<T> {
    private T content;
    private String signature;
    private String algorithm;
}
