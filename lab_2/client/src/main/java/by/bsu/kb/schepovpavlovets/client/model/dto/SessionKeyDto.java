package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Data;

@Data
public class SessionKeyDto {
    private String sessionKey;
    private String iv;
}
