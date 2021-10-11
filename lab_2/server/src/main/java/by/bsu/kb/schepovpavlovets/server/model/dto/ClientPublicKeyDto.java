package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class ClientPublicKeyDto {
    private String base64Key;
    private String base64KeyRsa;
}
