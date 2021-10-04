package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServerSignUpResponseDto {
    private String publicKey;
    private String sessionKey;
    private String clientId;
}
