package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DisconnectRequestDto {
    private String encodedClientId;
    private String encodedConnectionId;
}
