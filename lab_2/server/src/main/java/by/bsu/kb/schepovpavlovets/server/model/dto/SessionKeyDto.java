package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SessionKeyDto {
    private String sessionKey;
    private String iv;
}
