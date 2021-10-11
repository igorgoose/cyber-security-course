package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class DisconnectRequestDto {
    private String encodedClientId;
    private String encodedConnectionId;
}
