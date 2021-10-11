package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Data;

@Data
public class ServerConnectionResponseDto {
    private String connectionId;
    private String sessionKey;
    private String iv;
    private String expiresAt;
}
