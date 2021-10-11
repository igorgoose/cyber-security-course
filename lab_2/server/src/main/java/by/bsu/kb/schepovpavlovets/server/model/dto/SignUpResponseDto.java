package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SignUpResponseDto {
    private String publicKey;
    private String clientId;
}
