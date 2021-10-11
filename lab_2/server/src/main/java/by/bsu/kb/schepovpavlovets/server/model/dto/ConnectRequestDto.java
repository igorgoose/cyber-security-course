package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class ConnectRequestDto {
    private String encodedNamespace;
    private String encodedClientId;
}
