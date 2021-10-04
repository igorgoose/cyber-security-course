package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Data;

@Data
public class ServerSignUpResponseDto {
    private String publicKey;
    private String sessionKey;
    private String clientId;
}
