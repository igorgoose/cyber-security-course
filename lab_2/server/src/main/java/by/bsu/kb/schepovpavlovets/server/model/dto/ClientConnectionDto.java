package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientConnectionDto {
    private String connectionId;
    private String sessionKey;
    private String iv;
    private String expiresAt;
}
